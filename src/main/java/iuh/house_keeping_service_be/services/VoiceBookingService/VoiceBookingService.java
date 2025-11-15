package iuh.house_keeping_service_be.services.VoiceBookingService;

import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.VoiceBooking.*;
import iuh.house_keeping_service_be.models.Booking;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.VoiceBookingRequest;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.VoiceBookingRequestRepository;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
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

    @Value("${whisper.processing.async-enabled:true}")
    private boolean asyncEnabled;

    /**
     * Process voice booking request synchronously
     */
    @Transactional
    public VoiceBookingResponse processVoiceBooking(
            MultipartFile audioFile,
            String customerId,
            Map<String, Object> hints
    ) {
        log.info("Processing voice booking for customer: {}", customerId);

        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        // Create initial voice booking request record
        VoiceBookingRequest voiceRequest = createInitialVoiceRequest(customer, audioFile, hints);
        voiceRequest.markAsProcessing();
        voiceRequest = voiceBookingRequestRepository.save(voiceRequest);

        try {
            // Step 1: Voice to text conversion
            log.info("Starting voice-to-text conversion for request: {}", voiceRequest.getId());
            VoiceToTextResult voiceResult = voiceToTextService.transcribe(audioFile, "vi");

            if (!voiceResult.hasTranscript()) {
                voiceRequest.markAsFailed("Failed to transcribe audio");
                voiceBookingRequestRepository.save(voiceRequest);
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
                    customerId,
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

                return VoiceBookingResponse.partial(
                        voiceRequest.getId(),
                        voiceResult.transcript(),
                        parsedInfo.missingFields(),
                        parsedInfo.clarificationMessage(),
                        voiceResult.confidenceScore(),
                        (int) voiceResult.processingTimeMs()
                );

            } else {
                voiceRequest.markAsFailed("Failed to parse transcript");
                voiceBookingRequestRepository.save(voiceRequest);

                return VoiceBookingResponse.failed(
                        voiceRequest.getId(),
                        "Không thể phân tích yêu cầu từ giọng nói",
                        "Parsing failed with low confidence"
                );
            }

        } catch (Exception e) {
            log.error("Error processing voice booking: {}", e.getMessage(), e);
            voiceRequest.markAsFailed(e.getMessage());
            voiceBookingRequestRepository.save(voiceRequest);

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
            String customerId,
            Map<String, Object> hints
    ) {
        log.info("Processing voice booking asynchronously for customer: {}", customerId);
        VoiceBookingResponse response = processVoiceBooking(audioFile, customerId, hints);
        return CompletableFuture.completedFuture(response);
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

        // Try to get audio duration
        try {
            // This is a placeholder - actual duration calculation would need audio processing
            voiceRequest.setAudioDurationSeconds(BigDecimal.ZERO);
        } catch (Exception e) {
            log.warn("Could not calculate audio duration: {}", e.getMessage());
        }

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
