package iuh.house_keeping_service_be.services.VoiceBookingService;

import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.VoiceBooking.*;
import iuh.house_keeping_service_be.models.Booking;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.VoiceBookingRequest;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.VoiceBookingRequestRepository;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.VoiceBookingService.VoiceBookingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main service for voice booking workflow
 * Orchestrates voice-to-text, parsing, and booking creation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VoiceBookingService {

    private final VoiceToTextService voiceToTextService;
    private final VoiceBookingParserService parserService;
    private final BookingService bookingService;
    private final VoiceBookingRequestRepository voiceBookingRequestRepository;
    private final CustomerRepository customerRepository;
    private final VoiceBookingEventPublisher voiceBookingEventPublisher;

    @Value("${whisper.processing.async-enabled:true}")
    private boolean asyncEnabled;

    /**
     * Process voice booking request synchronously
     */
    @Transactional
    public VoiceBookingResponse processVoiceBooking(
            MultipartFile audioFile,
            String username,
            Map<String, Object> hints
    ) {
        log.info("Processing voice booking for customer: {}", username);

        // Find customer by username
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + username));

        // Create initial voice booking request record
        VoiceBookingRequest voiceRequest = createInitialVoiceRequest(customer, audioFile, hints);
        voiceRequest.markAsProcessing();
        voiceRequest = voiceBookingRequestRepository.save(voiceRequest);
        voiceBookingEventPublisher.emitReceived(voiceRequest.getId(), username);

        try {
            // Step 1: Voice to text conversion
            log.info("Starting voice-to-text conversion for request: {}", voiceRequest.getId());
            voiceBookingEventPublisher.emitTranscribing(voiceRequest.getId(), username, 0.1);
            VoiceToTextResult voiceResult = voiceToTextService.transcribe(audioFile, "vi");
            voiceBookingEventPublisher.emitTranscribing(voiceRequest.getId(), username, 1.0);

            if (!voiceResult.hasTranscript()) {
                voiceRequest.markAsFailed("Failed to transcribe audio");
                voiceBookingRequestRepository.save(voiceRequest);
                voiceBookingEventPublisher.emitFailed(
                        voiceRequest.getId(),
                        username,
                        "Không thể chuyển đổi giọng nói thành văn bản",
                        null,
                        null
                );
                return VoiceBookingResponse.failed(
                        voiceRequest.getId(),
                        "Không thể chuyển đổi giọng nói thành văn bản",
                        "No transcript generated"
                );
            }

            // Update voice request with transcript
            updateVoiceRequestWithTranscript(voiceRequest, voiceResult);

            // Step 2: Parse transcript into booking info
            log.info("Parsing transcript for request: {}", voiceRequest.getId());
            ParsedBookingInfo parsedInfo = parserService.parseTranscript(
                    voiceResult.transcript(),
                    customer.getCustomerId(),
                    hints
            );

            // Step 3: Handle parsing result
            if (parsedInfo.isComplete()) {
                // Create booking
                log.info("Creating booking from voice request: {}", voiceRequest.getId());
                BookingCreationSummary bookingSummary = bookingService.createBooking(parsedInfo.bookingRequest());

                if (bookingSummary.getBookingId() != null) {
                    // Mark as completed
                    Booking booking = new Booking();
                    booking.setBookingId(bookingSummary.getBookingId());
                    voiceRequest.markAsCompleted(booking);
                    voiceBookingRequestRepository.save(voiceRequest);
                    voiceBookingEventPublisher.emitCompleted(
                            voiceRequest.getId(),
                            username,
                            bookingSummary.getBookingId(),
                            voiceResult.transcript(),
                            (int) voiceResult.processingTimeMs()
                    );

                    return VoiceBookingResponse.completed(
                            voiceRequest.getId(),
                            bookingSummary.getBookingId(),
                            voiceResult.transcript(),
                            voiceResult.confidenceScore(),
                            (int) voiceResult.processingTimeMs()
                    );
                } else {
                    String errorMsg = "Booking creation failed";
                    voiceRequest.markAsFailed(errorMsg);
                    voiceBookingRequestRepository.save(voiceRequest);
                    voiceBookingEventPublisher.emitFailed(
                            voiceRequest.getId(),
                            username,
                            errorMsg,
                            voiceResult.transcript(),
                            (int) voiceResult.processingTimeMs()
                    );

                    return VoiceBookingResponse.failed(
                            voiceRequest.getId(),
                            "Không thể tạo booking",
                            errorMsg
                    );
                }

            } else if (parsedInfo.requiresClarification()) {
                // Mark as partial
                voiceRequest.markAsPartial(parsedInfo.missingFields());
                voiceBookingRequestRepository.save(voiceRequest);

                // Convert extractedFields to Map<String, Object> for better FE handling
                Map<String, Object> extractedInfo = new java.util.HashMap<>(parsedInfo.extractedFields());
                voiceBookingEventPublisher.emitPartial(
                        voiceRequest.getId(),
                        username,
                        voiceResult.transcript(),
                        parsedInfo.missingFields(),
                        parsedInfo.clarificationMessage(),
                        (int) voiceResult.processingTimeMs()
                );

                return VoiceBookingResponse.partial(
                        voiceRequest.getId(),
                        voiceResult.transcript(),
                        parsedInfo.missingFields(),
                        parsedInfo.clarificationMessage(),
                        extractedInfo,
                        voiceResult.confidenceScore(),
                        (int) voiceResult.processingTimeMs()
                );

            } else {
                voiceRequest.markAsFailed("Failed to parse transcript");
                voiceBookingRequestRepository.save(voiceRequest);
                voiceBookingEventPublisher.emitFailed(
                        voiceRequest.getId(),
                        username,
                        "Failed to parse transcript",
                        voiceResult.transcript(),
                        (int) voiceResult.processingTimeMs()
                );

                return VoiceBookingResponse.failed(
                        voiceRequest.getId(),
                        "Không thể phân tích yêu cầu từ giọng nói",
                        "Parsing failed with low confidence"
                );
            }

        } catch (iuh.house_keeping_service_be.exceptions.BookingValidationException e) {
            // Handle booking validation errors specifically
            String errorDetails = e.getErrors() != null && !e.getErrors().isEmpty() 
                    ? String.join("; ", e.getErrors())
                    : e.getMessage();
            log.error("Booking validation failed for voice request {}: {}", voiceRequest.getId(), errorDetails);
            voiceRequest.markAsFailed(errorDetails);
            voiceBookingRequestRepository.save(voiceRequest);
            voiceBookingEventPublisher.emitFailed(
                    voiceRequest.getId(),
                    username,
                    errorDetails,
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs()
            );

            return VoiceBookingResponse.failed(
                    voiceRequest.getId(),
                    "Không thể tạo booking: " + errorDetails,
                    errorDetails
            );
        } catch (Exception e) {
            log.error("Error processing voice booking: {}", e.getMessage(), e);
            voiceRequest.markAsFailed(e.getMessage());
            voiceBookingRequestRepository.save(voiceRequest);
            voiceBookingEventPublisher.emitFailed(
                    voiceRequest.getId(),
                    username,
                    e.getMessage(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs()
            );

            return VoiceBookingResponse.failed(
                    voiceRequest.getId(),
                    "Đã xảy ra lỗi khi xử lý yêu cầu đặt lịch bằng giọng nói",
                    e.getMessage()
            );
        }
    }

    /**
     * Process voice booking request asynchronously
     */
    @Async
    @Transactional
    public CompletableFuture<VoiceBookingResponse> processVoiceBookingAsync(
            MultipartFile audioFile,
            String username,
            Map<String, Object> hints
    ) {
        log.info("Processing voice booking asynchronously for customer: {}", username);
        VoiceBookingResponse response = processVoiceBooking(audioFile, username, hints);
        return CompletableFuture.completedFuture(response);
    }

    /**
     * Continue a partial voice booking with additional information
     */
    @Transactional
    public VoiceBookingResponse continueVoiceBooking(
            String requestId,
            MultipartFile audioFile,
            String additionalText,
            Map<String, String> explicitFields,
            String username
    ) {
        log.info("Continuing voice booking request: {}", requestId);

        // Find original voice request
        VoiceBookingRequest voiceRequest = voiceBookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Voice booking request not found: " + requestId));

        // Validate status
        if (!"PARTIAL".equals(voiceRequest.getStatus())) {
            VoiceBookingResponse failedResponse = VoiceBookingResponse.failed(
                    requestId,
                    "Không thể tiếp tục yêu cầu này",
                    "Request status is not PARTIAL, current status: " + voiceRequest.getStatus()
            );
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    "Request status is not PARTIAL, current status: " + voiceRequest.getStatus(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs()
            );
            return failedResponse;
        }

        // Verify customer ownership
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + username));
        
        if (!voiceRequest.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            VoiceBookingResponse failedResponse = VoiceBookingResponse.failed(
                    requestId,
                    "Không có quyền truy cập yêu cầu này",
                    "Customer mismatch"
            );
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    "Customer mismatch",
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs()
            );
            return failedResponse;
        }

        try {
            String originalTranscript = voiceRequest.getTranscript();
            String combinedTranscript = originalTranscript;
            
            // Process additional information sources
            if (audioFile != null && !audioFile.isEmpty()) {
                // Transcribe additional audio
                log.info("Transcribing additional audio for request: {}", requestId);
                voiceBookingEventPublisher.emitTranscribing(requestId, username, 0.2);
                VoiceToTextResult additionalVoice = voiceToTextService.transcribe(audioFile, "vi");
                voiceBookingEventPublisher.emitTranscribing(requestId, username, 1.0);
                
                if (additionalVoice.hasTranscript()) {
                    combinedTranscript = originalTranscript + " " + additionalVoice.transcript();
                    log.info("Combined transcript with audio: {}", combinedTranscript);
                }
            } else if (additionalText != null && !additionalText.isBlank()) {
                // Use provided text
                combinedTranscript = originalTranscript + " " + additionalText;
                log.info("Combined transcript with text: {}", combinedTranscript);
            }
            
            // Prepare enhanced hints with explicit fields
            Map<String, Object> enhancedHints = new java.util.HashMap<>(voiceRequest.getHints() != null 
                    ? voiceRequest.getHints() 
                    : new java.util.HashMap<>());
            
            if (explicitFields != null && !explicitFields.isEmpty()) {
                enhancedHints.put("explicitFields", explicitFields);
                log.info("Added explicit fields to hints: {}", explicitFields);
            }
            
            // Re-parse with combined information
            voiceRequest.markAsProcessing();
            voiceRequest.setTranscript(combinedTranscript);
            voiceBookingRequestRepository.save(voiceRequest);
            voiceBookingEventPublisher.emitReceived(requestId, username);
            
            ParsedBookingInfo parsedInfo = parserService.parseTranscript(
                    combinedTranscript,
                    customer.getCustomerId(),
                    enhancedHints
            );
            
            // Check if now complete
            if (parsedInfo.isComplete()) {
                log.info("Creating booking after continue for request: {}", requestId);
                BookingCreationSummary bookingSummary = bookingService.createBooking(parsedInfo.bookingRequest());
                
                if (bookingSummary.getBookingId() != null) {
                    Booking booking = new Booking();
                    booking.setBookingId(bookingSummary.getBookingId());
                    voiceRequest.markAsCompleted(booking);
                    voiceBookingRequestRepository.save(voiceRequest);
                    voiceBookingEventPublisher.emitCompleted(
                            requestId,
                            username,
                            bookingSummary.getBookingId(),
                            combinedTranscript,
                            voiceRequest.getProcessingTimeMs()
                    );
                    
                    return VoiceBookingResponse.completed(
                            requestId,
                            bookingSummary.getBookingId(),
                            combinedTranscript,
                            voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null,
                            voiceRequest.getProcessingTimeMs()
                    );
                } else {
                    String errorMsg = "Booking creation failed after continue";
                    voiceRequest.markAsFailed(errorMsg);
                    voiceBookingRequestRepository.save(voiceRequest);
                    voiceBookingEventPublisher.emitFailed(
                            requestId,
                            username,
                            errorMsg,
                            combinedTranscript,
                            voiceRequest.getProcessingTimeMs()
                    );
                    
                    return VoiceBookingResponse.failed(
                            requestId,
                            "Không thể tạo booking",
                            errorMsg
                    );
                }
            } else {
                // Still missing information
                voiceRequest.markAsPartial(parsedInfo.missingFields());
                voiceBookingRequestRepository.save(voiceRequest);
                
                Map<String, Object> extractedInfo = new java.util.HashMap<>(parsedInfo.extractedFields());
                voiceBookingEventPublisher.emitPartial(
                        requestId,
                        username,
                        combinedTranscript,
                        parsedInfo.missingFields(),
                        parsedInfo.clarificationMessage(),
                        voiceRequest.getProcessingTimeMs()
                );
                
                return VoiceBookingResponse.partial(
                        requestId,
                        combinedTranscript,
                        parsedInfo.missingFields(),
                        parsedInfo.clarificationMessage(),
                        extractedInfo,
                        voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null,
                        voiceRequest.getProcessingTimeMs()
                );
            }
            
        } catch (iuh.house_keeping_service_be.exceptions.BookingValidationException e) {
            String errorDetails = e.getErrors() != null && !e.getErrors().isEmpty() 
                    ? String.join("; ", e.getErrors())
                    : e.getMessage();
            log.error("Booking validation failed for continued request {}: {}", requestId, errorDetails);
            voiceRequest.markAsFailed(errorDetails);
            voiceBookingRequestRepository.save(voiceRequest);
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    errorDetails,
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs()
            );
            
            return VoiceBookingResponse.failed(
                    requestId,
                    "Không thể tạo booking: " + errorDetails,
                    errorDetails
            );
        } catch (Exception e) {
            log.error("Error continuing voice booking: {}", e.getMessage(), e);
            voiceRequest.markAsFailed(e.getMessage());
            voiceBookingRequestRepository.save(voiceRequest);
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    e.getMessage(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs()
            );
            
            return VoiceBookingResponse.failed(
                    requestId,
                    "Đã xảy ra lỗi khi tiếp tục xử lý yêu cầu",
                    e.getMessage()
            );
        }
    }

    /**
     * Get voice booking request by ID
     */
    public VoiceBookingRequest getVoiceBookingRequest(String requestId) {
        return voiceBookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Voice booking request not found: " + requestId));
    }

    /**
     * Check if voice booking is enabled
     */
    public boolean isVoiceBookingEnabled() {
        return voiceToTextService.isEnabled();
    }

    /**
     * Create initial voice booking request record
     */
    private VoiceBookingRequest createInitialVoiceRequest(
            Customer customer,
            MultipartFile audioFile,
            Map<String, Object> hints
    ) {
        VoiceBookingRequest voiceRequest = new VoiceBookingRequest();
        voiceRequest.setCustomer(customer);
        voiceRequest.setAudioFileName(audioFile.getOriginalFilename());
        voiceRequest.setAudioSizeBytes(audioFile.getSize());
        voiceRequest.setHints(hints);
        voiceRequest.setStatus("PENDING");
        voiceRequest.setTranscript(""); // Will be updated after transcription
        voiceRequest.setProcessingTimeMs(0); // Initialize to 0, will be updated after transcription
        voiceRequest.setAudioDurationSeconds(null); // Set null to pass constraint, will be updated after audio processing

        return voiceRequest;
    }

    /**
     * Update voice request with transcript results
     */
    private void updateVoiceRequestWithTranscript(
            VoiceBookingRequest voiceRequest,
            VoiceToTextResult voiceResult
    ) {
        voiceRequest.setTranscript(voiceResult.transcript());
        voiceRequest.setProcessingTimeMs((int) voiceResult.processingTimeMs());

        if (voiceResult.confidenceScore() != null) {
            voiceRequest.setConfidenceScore(BigDecimal.valueOf(voiceResult.confidenceScore()));
        }

        voiceBookingRequestRepository.save(voiceRequest);
    }
}
