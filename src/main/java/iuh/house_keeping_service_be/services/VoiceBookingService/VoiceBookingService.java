package iuh.house_keeping_service_be.services.VoiceBookingService;

import iuh.house_keeping_service_be.dtos.Booking.internal.BookingValidationResult;
import iuh.house_keeping_service_be.dtos.Booking.internal.ServiceValidationInfo;
import iuh.house_keeping_service_be.dtos.Booking.request.AssignmentRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingDetailRequest;
import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.ApiResponse;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeRequest;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeResponse;
import iuh.house_keeping_service_be.dtos.VoiceBooking.*;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.repositories.VoiceBookingRequestRepository;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.EmployeeScheduleService.EmployeeScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import iuh.house_keeping_service_be.utils.BookingDTOFormatter;

import java.text.Normalizer;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Main service for voice booking workflow
 * Orchestrates voice-to-text, parsing, and booking creation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VoiceBookingService {

    private final VoiceToTextService voiceToTextService;
    private final TextToSpeechService textToSpeechService;
    private final VoiceBookingParserService parserService;
    private final BookingService bookingService;
    private final VoiceBookingRequestRepository voiceBookingRequestRepository;
    private final CustomerRepository customerRepository;
    private final VoiceBookingEventPublisher voiceBookingEventPublisher;
    private final EmployeeScheduleService employeeScheduleService;
    private final EmployeeRepository employeeRepository;
    private final GeminiPromptService geminiPromptService;
    private static final List<String> INTENT_KEYWORDS = List.of(
            "dọn", "don", "dọn dẹp", "don dep", "lau", "lau dọn", "lau don",
            "vệ sinh", "ve sinh", "giúp việc", "giup viec", "housekeeping",
            "dọn nhà", "don nha", "dọn phòng", "don phong", "clean", "booking", "đặt lịch", "dat lich", "đặt", "dat", "lịch", "lich",
            "dịch vụ", "dich vu", "quét", "quet", "chà", "cha", "lau nhà", "lau nha"
    );
    private static final String MISSING_DETAIL_PROMPT = "Bạn vui lòng cung cấp thêm thông tin chi tiết về dịch vụ bạn muốn đặt cho chúng tôi.";
    private static final String AI_SERVICE_INSTRUCTION = "Bạn không được bịa đặt hay nói sai về tên các dịch vụ hiện có.";

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
        iuh.house_keeping_service_be.models.VoiceBookingRequest voiceRequest = createInitialVoiceRequest(customer, audioFile, hints);
        voiceRequest.markAsProcessing();
        voiceRequest = voiceBookingRequestRepository.save(voiceRequest);
        voiceBookingEventPublisher.emitReceived(voiceRequest.getId(), username, null, null);
        String aiPrompt = null;

        try {
            // Step 1: Voice to text conversion
            log.info("Starting voice-to-text conversion for request: {}", voiceRequest.getId());
            voiceBookingEventPublisher.emitTranscribing(voiceRequest.getId(), username, 0.1, null, null);
            VoiceToTextResult voiceResult = voiceToTextService.transcribe(audioFile, "vi");
            voiceBookingEventPublisher.emitTranscribing(voiceRequest.getId(), username, 1.0, null, null);
            aiPrompt = geminiPromptService.generatePromptWithInstruction(
                    voiceResult.transcript(),
                    buildAiInstruction(List.of("service", "bookingTime", "address"), null)
            ).orElse(null);
            if (StringUtils.hasText(aiPrompt)) {
                log.info("Gemini prompt generated for request {} ({} chars)", voiceRequest.getId(), aiPrompt.length());
            } else {
                log.debug("Gemini prompt skipped or empty for request {}", voiceRequest.getId());
            }

            if (!voiceResult.hasTranscript()) {
                voiceRequest.markAsFailed("Failed to transcribe audio");
                voiceBookingRequestRepository.save(voiceRequest);
                VoiceBookingResponse response = enrichFailure(VoiceBookingResponse.failed(
                        voiceRequest.getId(),
                        "Không thể chuyển đổi giọng nói thành văn bản",
                        "No transcript generated"
                ));
                VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
                voiceBookingEventPublisher.emitFailed(
                        voiceRequest.getId(),
                        username,
                        response.message(),
                        null,
                        voiceRequest.getProcessingTimeMs(),
                        response.message(),
                        speech,
                        defaultFailureHints(),
                        3000
                );
                return attachSpeech(response, speech);
            }

            String rawTranscript = voiceResult.transcript();
            String sanitizedTranscript = sanitizeTranscriptForBooking(rawTranscript);
            boolean hasBookingIntent = StringUtils.hasText(sanitizedTranscript);
            if (!hasBookingIntent && StringUtils.hasText(rawTranscript)) {
                log.info("Transcript for request {} has no booking/service intent, clearing transcript output", voiceRequest.getId());
            }

            // Step 1.5: Detect booking intent via keywords
            if (!hasBookingIntent) {
                // Treat as a booking intent without details -> partial and ask for details
                List<String> missing = List.of("service", "bookingTime", "address");
                Map<String, Object> extractedInfo = new HashMap<>();
                if (StringUtils.hasText(rawTranscript)) {
                    extractedInfo.put("note", rawTranscript);
                }
                String baseClarification = MISSING_DETAIL_PROMPT;
                boolean allowAiPrompt = allowAiPromptForMissing(missing) && shouldUseAiPrompt(missing, extractedInfo);
                String instruction = buildAiInstruction(missing, extractedInfo);
                String localAiPrompt = allowAiPrompt
                        ? geminiPromptService.generatePromptWithInstruction(baseClarification, instruction).orElse(null)
                        : null;

                voiceRequest.markAsPartial(missing);
                voiceRequest.setTranscript(sanitizedTranscript);
                voiceRequest.setProcessingTimeMs((int) voiceResult.processingTimeMs());
                voiceBookingRequestRepository.save(voiceRequest);

                VoiceBookingResponse response = VoiceBookingResponse.partial(
                        voiceRequest.getId(),
                        sanitizedTranscript,
                        missing,
                        baseClarification,
                        extractedInfo,
                        voiceResult.confidenceScore(),
                        (int) voiceResult.processingTimeMs()
                );
                String speakMessage = baseClarification;
                if (allowAiPrompt && StringUtils.hasText(localAiPrompt)) {
                    extractedInfo.put("aiPrompt", localAiPrompt);
                    response = response.toBuilder()
                            .message(localAiPrompt)
                            .clarificationMessage(baseClarification)
                            .build();
                    speakMessage = localAiPrompt;
                }
                VoiceBookingSpeechPayload speech = buildSpeechPayload(
                        speakMessage,
                        response.clarificationMessage()
                );
                voiceBookingEventPublisher.emitPartial(
                        voiceRequest.getId(),
                        username,
                        sanitizedTranscript,
                        missing,
                        response.clarificationMessage(),
                        (int) voiceResult.processingTimeMs(),
                        response.message(),
                        speech,
                        false,
                        voiceResult.confidenceScore()
                );
                return attachSpeech(response, speech);
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
                log.info("Voice booking request {} parsed successfully, preparing preview", voiceRequest.getId());
                VoiceBookingDraftResult draftResult = prepareDraft(customer, parsedInfo.bookingRequest());
                voiceRequest.markAsAwaitingConfirmation(draftResult.bookingRequest(), draftResult.preview());
                voiceRequest = voiceBookingRequestRepository.save(voiceRequest);
                VoiceBookingResponse response = VoiceBookingResponse.awaitingConfirmation(
                        voiceRequest.getId(),
                        voiceResult.transcript(),
                        draftResult.preview(),
                        voiceResult.confidenceScore(),
                        (int) voiceResult.processingTimeMs()
                );
                String speakMessage = pickTtsMessage(aiPrompt, response.message(), response.clarificationMessage());
                if (StringUtils.hasText(aiPrompt)) {
                    response = response.toBuilder().message(aiPrompt).build();
                }
                VoiceBookingSpeechPayload speech = buildSpeechPayload(speakMessage, response.clarificationMessage());
                voiceBookingEventPublisher.emitAwaitingConfirmation(
                        voiceRequest.getId(),
                        username,
                        draftResult.preview(),
                        response.processingTimeMs(),
                        response.message(),
                        speech,
                        true,
                        voiceResult.confidenceScore()
                );
                return attachSpeech(response, speech);
            } else if (parsedInfo.requiresClarification()) {
                // Mark as partial
                voiceRequest.markAsPartial(parsedInfo.missingFields());
                voiceBookingRequestRepository.save(voiceRequest);

                // Convert extractedFields to Map<String, Object> for better FE handling
                Map<String, Object> extractedInfo = new java.util.HashMap<>(parsedInfo.extractedFields());
                String baseClarification = buildClarificationText(parsedInfo.missingFields(), extractedInfo, parsedInfo.clarificationMessage());
                boolean missingServiceOnly = parsedInfo.missingFields().size() == 1 && parsedInfo.missingFields().contains("service");
                boolean allowAi = allowAiPromptForMissing(parsedInfo.missingFields());
                String localAiPrompt = aiPrompt;
                if (!shouldUseAiPrompt(parsedInfo.missingFields(), extractedInfo)) {
                    allowAi = false;
                    localAiPrompt = null;
                }
                if (missingServiceOnly) {
                    String instruction = buildServiceAiInstruction(extractedInfo);
                    localAiPrompt = geminiPromptService.generatePromptWithInstruction(
                            baseClarification,
                            instruction
                    ).orElse(localAiPrompt);
                    allowAi = true;
                } else {
                    String instruction = buildAiInstruction(parsedInfo.missingFields(), extractedInfo);
                    localAiPrompt = allowAi
                            ? geminiPromptService.generatePromptWithInstruction(baseClarification, instruction).orElse(localAiPrompt)
                            : localAiPrompt;
                }
                if (allowAi && StringUtils.hasText(localAiPrompt)) {
                    extractedInfo.put("aiPrompt", localAiPrompt);
                }
                VoiceBookingResponse response = VoiceBookingResponse.partial(
                        voiceRequest.getId(),
                        voiceResult.transcript(),
                        parsedInfo.missingFields(),
                        baseClarification,
                        extractedInfo,
                        voiceResult.confidenceScore(),
                        (int) voiceResult.processingTimeMs()
                );
                if (!Objects.equals(response.message(), baseClarification)) {
                    response = response.toBuilder()
                            .message(baseClarification)
                            .clarificationMessage(baseClarification)
                            .build();
                }
                String speakMessage = baseClarification;
                if (allowAi && StringUtils.hasText(localAiPrompt)) {
                    response = response.toBuilder().message(localAiPrompt).build();
                    speakMessage = localAiPrompt;
                }
                VoiceBookingSpeechPayload speech = buildSpeechPayload(
                        speakMessage,
                        response.clarificationMessage()
                );
                voiceBookingEventPublisher.emitPartial(
                        voiceRequest.getId(),
                        username,
                        voiceResult.transcript(),
                        parsedInfo.missingFields(),
                        parsedInfo.clarificationMessage(),
                        response.processingTimeMs(),
                        response.message(),
                        speech,
                        false,
                        voiceResult.confidenceScore()
                );

                return attachSpeech(response, speech);

            } else {
                voiceRequest.markAsFailed("Failed to parse transcript");
                voiceBookingRequestRepository.save(voiceRequest);
                VoiceBookingResponse response = enrichFailure(VoiceBookingResponse.failed(
                        voiceRequest.getId(),
                        "Không thể phân tích yêu cầu từ giọng nói",
                        "Parsing failed with low confidence"
                ));
                VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
                voiceBookingEventPublisher.emitFailed(
                        voiceRequest.getId(),
                        username,
                        "Failed to parse transcript",
                        voiceResult.transcript(),
                        (int) voiceResult.processingTimeMs(),
                        response.message(),
                        speech,
                        defaultFailureHints(),
                        3000
                );

                return attachSpeech(response, speech);
            }

        } catch (iuh.house_keeping_service_be.exceptions.BookingValidationException e) {
            // Handle booking validation errors specifically
            String errorDetails = e.getErrors() != null && !e.getErrors().isEmpty() 
                    ? String.join("; ", e.getErrors())
                    : e.getMessage();
            log.error("Booking validation failed for voice request {}: {}", voiceRequest.getId(), errorDetails);
            voiceRequest.markAsFailed(errorDetails);
            voiceBookingRequestRepository.save(voiceRequest);
            VoiceBookingResponse response = enrichFailure(VoiceBookingResponse.failed(
                    voiceRequest.getId(),
                    "Không thể tạo booking: " + errorDetails,
                    errorDetails
            ));
            VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
            voiceBookingEventPublisher.emitFailed(
                    voiceRequest.getId(),
                    username,
                    errorDetails,
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    response.message(),
                    speech,
                    defaultFailureHints(),
                    3000
            );

            return attachSpeech(response, speech);
        } catch (Exception e) {
            log.error("Error processing voice booking: {}", e.getMessage(), e);
            voiceRequest.markAsFailed(e.getMessage());
            voiceBookingRequestRepository.save(voiceRequest);
            VoiceBookingResponse response = enrichFailure(VoiceBookingResponse.failed(
                    voiceRequest.getId(),
                    "Đã xảy ra lỗi khi xử lý yêu cầu đặt lịch bằng giọng nói",
                    e.getMessage()
            ));
            VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
            voiceBookingEventPublisher.emitFailed(
                    voiceRequest.getId(),
                    username,
                    e.getMessage(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    response.message(),
                    speech,
                    defaultFailureHints(),
                    3000
            );

            return attachSpeech(response, speech);
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
        iuh.house_keeping_service_be.models.VoiceBookingRequest voiceRequest = voiceBookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Voice booking request not found: " + requestId));

        // Validate status
        if (!"PARTIAL".equals(voiceRequest.getStatus()) && !"AWAITING_CONFIRMATION".equals(voiceRequest.getStatus())) {
            VoiceBookingResponse failedResponse = enrichFailure(VoiceBookingResponse.failed(
                    requestId,
                    "Không thể tiếp tục yêu cầu này",
                    "Request status is not PARTIAL, current status: " + voiceRequest.getStatus()
            ));
            VoiceBookingSpeechPayload speech = buildSpeechPayload(failedResponse.message(), failedResponse.clarificationMessage());
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    "Request status is not PARTIAL, current status: " + voiceRequest.getStatus(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    failedResponse.message(),
                    speech,
                    defaultFailureHints(),
                    3000
            );
            return attachSpeech(failedResponse, speech);
        }

        // Verify customer ownership
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + username));
        
        if (!voiceRequest.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            VoiceBookingResponse failedResponse = enrichFailure(VoiceBookingResponse.failed(
                    requestId,
                    "Không có quyền truy cập yêu cầu này",
                    "Customer mismatch"
            ));
            VoiceBookingSpeechPayload speech = buildSpeechPayload(failedResponse.message(), failedResponse.clarificationMessage());
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    "Customer mismatch",
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    failedResponse.message(),
                    speech,
                    defaultFailureHints(),
                    3000
            );
            return attachSpeech(failedResponse, speech);
        }

        try {
            String originalTranscript = voiceRequest.getTranscript();
            StringBuilder combinedBuilder = new StringBuilder();
            if (StringUtils.hasText(originalTranscript)) {
                combinedBuilder.append(originalTranscript.trim());
            }
            boolean hasIntent = containsBookingIntent(combinedBuilder.toString());
            
            // Process additional information sources
            if (audioFile != null && !audioFile.isEmpty()) {
                // Transcribe additional audio
                log.info("Transcribing additional audio for request: {}", requestId);
                voiceBookingEventPublisher.emitTranscribing(requestId, username, 0.2, null, null);
                VoiceToTextResult additionalVoice = voiceToTextService.transcribe(audioFile, "vi");
                voiceBookingEventPublisher.emitTranscribing(requestId, username, 1.0, null, null);
                
                if (additionalVoice.hasTranscript()) {
                    String additionalRaw = additionalVoice.transcript().trim();
                    String toAppend = pickAppendableChunk(additionalRaw, hasIntent);
                    if (StringUtils.hasText(toAppend)) {
                        if (combinedBuilder.length() > 0) {
                            combinedBuilder.append(' ');
                        }
                        combinedBuilder.append(toAppend);
                        hasIntent = hasIntent || containsBookingIntent(toAppend);
                        log.info("Combined transcript with audio: {}", combinedBuilder);
                    } else {
                        log.info("Additional audio transcript for request {} has no booking/service intent, ignoring content", requestId);
                    }
                }
            }

            if (additionalText != null && !additionalText.isBlank()) {
                // Use provided text
                String toAppend = pickAppendableChunk(additionalText.trim(), hasIntent);
                if (combinedBuilder.length() > 0) {
                    combinedBuilder.append(' ');
                }
                if (StringUtils.hasText(toAppend)) {
                    combinedBuilder.append(toAppend);
                    hasIntent = hasIntent || containsBookingIntent(toAppend);
                    log.info("Combined transcript with text: {}", combinedBuilder);
                } else {
                    log.info("Additional text for request {} has no booking/service intent, ignoring content", requestId);
                }
            }

            String combinedTranscript = combinedBuilder.toString().trim();
            String sanitizedCombined = sanitizeTranscriptForBooking(combinedTranscript);
            if (!StringUtils.hasText(sanitizedCombined) && StringUtils.hasText(combinedTranscript)) {
                log.info("Combined transcript for request {} has no booking/service intent, clearing transcript output", requestId);
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
            voiceRequest.setTranscript(sanitizedCombined);
            voiceBookingRequestRepository.save(voiceRequest);
            voiceBookingEventPublisher.emitReceived(requestId, username, null, null);
            
            ParsedBookingInfo parsedInfo = parserService.parseTranscript(
                    sanitizedCombined,
                    customer.getCustomerId(),
                    enhancedHints
            );
            String aiPrompt = geminiPromptService.generatePromptWithInstruction(
                    sanitizedCombined,
                    buildAiInstruction(List.of("service", "bookingTime", "address"), null)
            ).orElse(null);
            
            // Check if now complete
            if (parsedInfo.isComplete()) {
                log.info("Parsed update successfully for request {}, refreshing preview", requestId);
                VoiceBookingDraftResult draftResult = prepareDraft(customer, parsedInfo.bookingRequest());
                voiceRequest.markAsAwaitingConfirmation(draftResult.bookingRequest(), draftResult.preview());
                voiceRequest = voiceBookingRequestRepository.save(voiceRequest);
                VoiceBookingResponse response = VoiceBookingResponse.awaitingConfirmation(
                        requestId,
                        sanitizedCombined,
                        draftResult.preview(),
                        voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null,
                        voiceRequest.getProcessingTimeMs()
                );
                String speakMessage = pickTtsMessage(aiPrompt, response.message(), response.clarificationMessage());
                if (StringUtils.hasText(aiPrompt)) {
                    response = response.toBuilder().message(aiPrompt).build();
                }
                VoiceBookingSpeechPayload speech = buildSpeechPayload(speakMessage, response.clarificationMessage());
                voiceBookingEventPublisher.emitAwaitingConfirmation(
                        requestId,
                        username,
                        draftResult.preview(),
                        voiceRequest.getProcessingTimeMs(),
                        response.message(),
                        speech,
                        true,
                        voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null
                );

                return attachSpeech(response, speech);
            } else {
                // Still missing information
                voiceRequest.markAsPartial(parsedInfo.missingFields());
                voiceBookingRequestRepository.save(voiceRequest);
                
                Map<String, Object> extractedInfo = new java.util.HashMap<>(parsedInfo.extractedFields());
                String baseClarification = buildClarificationText(parsedInfo.missingFields(), extractedInfo, parsedInfo.clarificationMessage());
                boolean missingServiceOnly = parsedInfo.missingFields().size() == 1 && parsedInfo.missingFields().contains("service");
                boolean allowAi = allowAiPromptForMissing(parsedInfo.missingFields())
                        && shouldUseAiPrompt(parsedInfo.missingFields(), extractedInfo);
                String localAiPrompt = allowAi ? aiPrompt : null;

                if (missingServiceOnly) {
                    String instruction = buildServiceAiInstruction(extractedInfo);
                    localAiPrompt = geminiPromptService.generatePromptWithInstruction(
                            baseClarification,
                            instruction
                    ).orElse(localAiPrompt);
                    allowAi = true;
                } else if (allowAi) {
                    String instruction = buildAiInstruction(parsedInfo.missingFields(), extractedInfo);
                    localAiPrompt = geminiPromptService.generatePromptWithInstruction(
                            baseClarification,
                            instruction
                    ).orElse(localAiPrompt);
                }

                if (allowAi && StringUtils.hasText(localAiPrompt)) {
                    extractedInfo.put("aiPrompt", localAiPrompt);
                }
                String clarificationMsg = baseClarification;
                VoiceBookingResponse response = VoiceBookingResponse.partial(
                        requestId,
                        sanitizedCombined,
                        parsedInfo.missingFields(),
                        clarificationMsg,
                        extractedInfo,
                        voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null,
                        voiceRequest.getProcessingTimeMs()
                );
                if (!Objects.equals(response.message(), clarificationMsg)) {
                    response = response.toBuilder()
                            .message(clarificationMsg)
                            .clarificationMessage(clarificationMsg)
                            .build();
                }
                String speakMessage = clarificationMsg;
                if (allowAi && StringUtils.hasText(localAiPrompt)) {
                    response = response.toBuilder().message(localAiPrompt).build();
                    speakMessage = localAiPrompt;
                }
                VoiceBookingSpeechPayload speech = buildSpeechPayload(
                        speakMessage,
                        response.clarificationMessage()
                );
                voiceBookingEventPublisher.emitPartial(
                        requestId,
                        username,
                        combinedTranscript,
                        parsedInfo.missingFields(),
                        response.clarificationMessage(),
                        voiceRequest.getProcessingTimeMs(),
                        response.message(),
                        speech,
                        false,
                        voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null
                );
                
                return attachSpeech(response, speech);
            }
            
        } catch (iuh.house_keeping_service_be.exceptions.BookingValidationException e) {
            String errorDetails = e.getErrors() != null && !e.getErrors().isEmpty() 
                    ? String.join("; ", e.getErrors())
                    : e.getMessage();
            log.error("Booking validation failed for continued request {}: {}", requestId, errorDetails);
            voiceRequest.markAsFailed(errorDetails);
            voiceBookingRequestRepository.save(voiceRequest);
            VoiceBookingResponse response = enrichFailure(VoiceBookingResponse.failed(
                    requestId,
                    "Không thể tạo booking: " + errorDetails,
                    errorDetails
            ));
            VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    errorDetails,
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    response.message(),
                    speech,
                    defaultFailureHints(),
                    3000
            );
            
            return attachSpeech(response, speech);
        } catch (Exception e) {
            log.error("Error continuing voice booking: {}", e.getMessage(), e);
            voiceRequest.markAsFailed(e.getMessage());
            voiceBookingRequestRepository.save(voiceRequest);
            VoiceBookingResponse response = enrichFailure(VoiceBookingResponse.failed(
                    requestId,
                    "Đã xảy ra lỗi khi tiếp tục xử lý yêu cầu",
                    e.getMessage()
            ));
            VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    e.getMessage(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    response.message(),
                    speech,
                    defaultFailureHints(),
                    3000
            );
            
            return attachSpeech(response, speech);
        }
    }

    /**
     * Persist the pending draft into an actual booking once the customer confirms.
     */
    @Transactional
    public VoiceBookingResponse confirmVoiceBooking(String requestId, String username) {
        log.info("Confirming voice booking draft {}", requestId);

        iuh.house_keeping_service_be.models.VoiceBookingRequest voiceRequest = voiceBookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Voice booking request not found: " + requestId));

        String currentStatus = voiceRequest.getStatus();
        log.info("Voice booking request {} has status: {}", requestId, currentStatus);
        
        if (!"AWAITING_CONFIRMATION".equals(currentStatus)) {
            String errorMessage = buildConfirmationErrorMessage(currentStatus);
            log.warn("Cannot confirm voice booking {}: current status is {}", requestId, currentStatus);
            throw new IllegalStateException(errorMessage);
        }

        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + username));

        if (!voiceRequest.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new IllegalArgumentException("Customer mismatch");
        }

        if (voiceRequest.getDraftBookingRequest() == null) {
            throw new IllegalStateException("Draft booking request is missing");
        }

        try {
            BookingCreationSummary bookingSummary = bookingService.createBooking(voiceRequest.getDraftBookingRequest());

            if (bookingSummary.getBookingId() == null) {
                throw new IllegalStateException("Booking creation failed");
            }

            Booking booking = new Booking();
            booking.setBookingId(bookingSummary.getBookingId());
            voiceRequest.markAsCompleted(booking);
            voiceRequest = voiceBookingRequestRepository.save(voiceRequest);
            VoiceBookingResponse response = VoiceBookingResponse.completed(
                    requestId,
                    bookingSummary.getBookingId(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null,
                    voiceRequest.getProcessingTimeMs()
            );
            VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
            voiceBookingEventPublisher.emitCompleted(
                    requestId,
                    username,
                    bookingSummary.getBookingId(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    response.message(),
                    speech,
                    voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null
            );

            return attachSpeech(response, speech);
        } catch (iuh.house_keeping_service_be.exceptions.BookingValidationException e) {
            log.error("Booking validation failed during confirmation {}: {}", requestId, e.getMessage());
            voiceRequest.markAsFailed(e.getMessage());
            voiceBookingRequestRepository.save(voiceRequest);
            VoiceBookingResponse response = enrichFailure(VoiceBookingResponse.failed(
                    requestId,
                    "Không thể tạo booking: " + e.getMessage(),
                    e.getMessage()
            ));
            VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    e.getMessage(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    response.message(),
                    speech,
                    defaultFailureHints(),
                    3000
            );

            return attachSpeech(response, speech);
        } catch (Exception e) {
            log.error("Unexpected error confirming booking {}: {}", requestId, e.getMessage(), e);
            voiceRequest.markAsFailed(e.getMessage());
            voiceBookingRequestRepository.save(voiceRequest);
            VoiceBookingResponse response = enrichFailure(VoiceBookingResponse.failed(
                    requestId,
                    "Đã xảy ra lỗi khi xác nhận đơn",
                    e.getMessage()
            ));
            VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
            voiceBookingEventPublisher.emitFailed(
                    requestId,
                    username,
                    e.getMessage(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs(),
                    response.message(),
                    speech,
                    defaultFailureHints(),
                    3000
            );

            return attachSpeech(response, speech);
        }
    }

    /**
     * Cancel a voice booking draft and remove temporary data.
     */
    @Transactional
    public VoiceBookingResponse cancelVoiceBooking(String requestId, String username) {
        log.info("Cancelling voice booking draft {}", requestId);

        iuh.house_keeping_service_be.models.VoiceBookingRequest voiceRequest = voiceBookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Voice booking request not found: " + requestId));

        if (!"AWAITING_CONFIRMATION".equals(voiceRequest.getStatus()) && !"PARTIAL".equals(voiceRequest.getStatus())) {
            throw new IllegalStateException("Only draft or partial voice booking requests can be cancelled");
        }

        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + username));

        if (!voiceRequest.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new IllegalArgumentException("Customer mismatch");
        }

        voiceRequest.markAsCancelled();
        VoiceBookingResponse response = VoiceBookingResponse.cancelled(requestId);
        VoiceBookingSpeechPayload speech = buildSpeechPayload(response.message(), response.clarificationMessage());
        voiceBookingEventPublisher.emitCancelled(requestId, username, response.message(), speech);
        voiceBookingRequestRepository.delete(voiceRequest);

        return attachSpeech(response, speech);
    }

    /**
     * Get voice booking request by ID ensuring ownership.
     */
    public iuh.house_keeping_service_be.models.VoiceBookingRequest getVoiceBookingRequest(
            String requestId,
            String username
    ) {
        iuh.house_keeping_service_be.models.VoiceBookingRequest voiceBookingRequest = voiceBookingRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Voice booking request not found: " + requestId));

        String ownerUsername = voiceBookingRequest.getCustomer() != null
                && voiceBookingRequest.getCustomer().getAccount() != null
                ? voiceBookingRequest.getCustomer().getAccount().getUsername()
                : null;

        if (ownerUsername == null || !ownerUsername.equals(username)) {
            throw new IllegalArgumentException("Voice booking request not found: " + requestId);
        }

        return voiceBookingRequest;
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
    private iuh.house_keeping_service_be.models.VoiceBookingRequest createInitialVoiceRequest(
            Customer customer,
            MultipartFile audioFile,
            Map<String, Object> hints
    ) {
        iuh.house_keeping_service_be.models.VoiceBookingRequest voiceRequest = new iuh.house_keeping_service_be.models.VoiceBookingRequest();
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

    private VoiceBookingDraftResult prepareDraft(Customer customer, BookingCreateRequest bookingRequest) {
        BookingValidationResult initialValidation = bookingService.validateBooking(bookingRequest);

        if (!initialValidation.isValid()) {
            throw iuh.house_keeping_service_be.exceptions.BookingValidationException.withErrors(
                    initialValidation.getErrors() != null && !initialValidation.getErrors().isEmpty()
                            ? initialValidation.getErrors()
                            : List.of("Không thể dựng đơn từ thông tin hiện tại")
            );
        }

        AutoAssignmentResult assignmentResult = ensureAssignments(customer, bookingRequest, initialValidation);
        BookingValidationResult finalValidation = bookingService.validateBooking(assignmentResult.bookingRequest());

        if (!finalValidation.isValid()) {
            throw iuh.house_keeping_service_be.exceptions.BookingValidationException.withErrors(
                    finalValidation.getErrors() != null && !finalValidation.getErrors().isEmpty()
                            ? finalValidation.getErrors()
                            : List.of("Thông tin booking không còn hợp lệ")
            );
        }

        VoiceBookingPreview preview = buildPreview(
                assignmentResult.bookingRequest(),
                finalValidation,
                assignmentResult.employeePreviews(),
                assignmentResult.autoAssigned()
        );

        return new VoiceBookingDraftResult(assignmentResult.bookingRequest(), preview);
    }

    private AutoAssignmentResult ensureAssignments(
            Customer customer,
            BookingCreateRequest bookingRequest,
            BookingValidationResult validationResult
    ) {
        List<AssignmentRequest> assignments = bookingRequest.assignments() != null
                ? new ArrayList<>(bookingRequest.assignments())
                : new ArrayList<>();

        Map<String, SuitableEmployeeResponse> autoAssignedSources = new HashMap<>();
        boolean autoAssigned = false;

        if (assignments.isEmpty()) {
            Address address = validationResult.getAddress();
            if (address == null) {
                throw new IllegalArgumentException("Không xác định được địa chỉ để chọn nhân viên");
            }

            List<AssignmentRequest> autoAssignments = new ArrayList<>();
            for (BookingDetailRequest detail : bookingRequest.bookingDetails()) {
                SuitableEmployeeRequest employeeRequest = new SuitableEmployeeRequest(
                        detail.serviceId(),
                        bookingRequest.bookingTime(),
                        address.getWard(),
                        address.getCity(),
                        customer.getCustomerId(),
                        null
                );

                ApiResponse<List<SuitableEmployeeResponse>> response = employeeScheduleService.findSuitableEmployees(employeeRequest);
                if (response.success() && response.data() != null && !response.data().isEmpty()) {
                    SuitableEmployeeResponse selected = response.data().get(0);
                    autoAssignments.add(new AssignmentRequest(selected.employeeId(), detail.serviceId()));
                    autoAssignedSources.put(selected.employeeId(), selected);
                    log.info("Auto-assigned employee {} ({}) for service {}", selected.employeeId(), selected.fullName(), detail.serviceId());
                } else {
                    log.warn("No suitable employee found for service {} at {}", detail.serviceId(), bookingRequest.bookingTime());
                }
            }

            if (!autoAssignments.isEmpty()) {
                assignments = autoAssignments;
                autoAssigned = true;
            }
        }

        BookingCreateRequest normalizedRequest = updateAssignments(bookingRequest, assignments);
        List<VoiceBookingEmployeePreview> employeePreviews = buildEmployeePreview(assignments, autoAssignedSources, autoAssigned);

        return new AutoAssignmentResult(normalizedRequest, employeePreviews, autoAssigned);
    }

    private BookingCreateRequest updateAssignments(BookingCreateRequest original, List<AssignmentRequest> assignments) {
        List<AssignmentRequest> normalizedAssignments = (assignments == null || assignments.isEmpty()) ? null : assignments;

        return new BookingCreateRequest(
                original.addressId(),
                original.newAddress(),
                original.bookingTime(),
                original.note(),
                original.title(),
                original.imageUrls(),
                original.promoCode(),
                original.bookingDetails(),
                normalizedAssignments,
                original.paymentMethodId(),
                original.additionalFeeIds()
        );
    }

    private List<VoiceBookingEmployeePreview> buildEmployeePreview(
            List<AssignmentRequest> assignments,
            Map<String, SuitableEmployeeResponse> autoAssignedSources,
            boolean autoAssigned
    ) {
        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }

        Map<String, List<Integer>> serviceIdsByEmployee = new LinkedHashMap<>();
        assignments.stream()
                .filter(assignment -> assignment.employeeId() != null)
                .forEach(assignment -> serviceIdsByEmployee
                        .computeIfAbsent(assignment.employeeId(), key -> new ArrayList<>())
                        .add(assignment.serviceId()));

        List<VoiceBookingEmployeePreview> previews = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : serviceIdsByEmployee.entrySet()) {
            String employeeId = entry.getKey();
            List<Integer> serviceIds = entry.getValue();
            SuitableEmployeeResponse autoSource = autoAssignedSources.get(employeeId);

            if (autoSource != null) {
                previews.add(new VoiceBookingEmployeePreview(
                        employeeId,
                        autoSource.fullName(),
                        autoSource.avatar(),
                        autoSource.rating(),
                        autoSource.hasWorkedWithCustomer(),
                        serviceIds,
                        true
                ));
                continue;
            }

            employeeRepository.findById(employeeId).ifPresentOrElse(employee ->
                            previews.add(new VoiceBookingEmployeePreview(
                                    employeeId,
                                    employee.getFullName(),
                                    employee.getAvatar(),
                                    employee.getRating() != null ? employee.getRating().name() : null,
                                    null,
                                    serviceIds,
                                    autoAssigned
                            )),
                    () -> log.warn("Employee {} not found while building preview", employeeId)
            );
        }

        return previews;
    }

    private VoiceBookingPreview buildPreview(
            BookingCreateRequest bookingRequest,
            BookingValidationResult validation,
            List<VoiceBookingEmployeePreview> employees,
            boolean autoAssignedEmployees
    ) {
        Address address = validation.getAddress();
        List<ServiceValidationInfo> serviceValidations = validation.getServiceValidations() != null
                ? validation.getServiceValidations()
                : List.of();

        List<VoiceBookingPreviewServiceItem> serviceItems = new ArrayList<>();
        for (int i = 0; i < bookingRequest.bookingDetails().size(); i++) {
            BookingDetailRequest detail = bookingRequest.bookingDetails().get(i);
            ServiceValidationInfo serviceInfo = i < serviceValidations.size() ? serviceValidations.get(i) : null;

            BigDecimal unitPrice = detail.expectedPricePerUnit();
            BigDecimal subtotal = (serviceInfo != null && serviceInfo.getCalculatedPrice() != null)
                    ? serviceInfo.getCalculatedPrice()
                    : detail.expectedPrice();

            serviceItems.add(new VoiceBookingPreviewServiceItem(
                    detail.serviceId(),
                    serviceInfo != null ? serviceInfo.getServiceName() : null,
                    detail.quantity(),
                    unitPrice,
                    BookingDTOFormatter.formatPrice(unitPrice),
                    subtotal,
                    BookingDTOFormatter.formatPrice(subtotal),
                    detail.selectedChoiceIds()
            ));
        }

        BigDecimal totalAmount = validation.getCalculatedTotalAmount() != null
                ? validation.getCalculatedTotalAmount()
                : BigDecimal.ZERO;

        return new VoiceBookingPreview(
                bookingRequest.addressId(),
                address != null ? address.getFullAddress() : null,
                address != null ? address.getWard() : null,
                address != null ? address.getCity() : null,
                bookingRequest.bookingTime(),
                bookingRequest.note(),
                bookingRequest.promoCode(),
                bookingRequest.paymentMethodId(),
                totalAmount,
                BookingDTOFormatter.formatPrice(totalAmount),
                serviceItems,
                employees,
                autoAssignedEmployees
        );
    }

    private VoiceBookingResponse attachSpeech(VoiceBookingResponse response, VoiceBookingSpeechPayload speech) {
        if (response == null || speech == null || !speech.hasAudio()) {
            return response;
        }
        return response.withSpeech(speech);
    }

    private VoiceBookingSpeechPayload buildSpeechPayload(String message, String clarification) {
        if (textToSpeechService == null || !textToSpeechService.isEnabled()) {
            return null;
        }

        TextToSpeechResult messageAudio = synthesizeSafe(message);
        TextToSpeechResult clarificationAudio = synthesizeSafe(
                StringUtils.hasText(clarification) && !clarification.equals(message) ? clarification : null
        );

        // Deduplicate: if clarification text collapses to same audio, keep only one
        if (messageAudio != null && clarificationAudio != null
                && Objects.equals(messageAudio.audioUrl(), clarificationAudio.audioUrl())) {
            clarificationAudio = null;
        }

        if (messageAudio == null && clarificationAudio == null) {
            return null;
        }

        return VoiceBookingSpeechPayload.builder()
                .message(messageAudio)
                .clarification(clarificationAudio)
                .build();
    }

    private TextToSpeechResult synthesizeSafe(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return textToSpeechService.synthesize(text).orElse(null);
        } catch (Exception ex) {
            log.warn("TTS synthesis failed: {}", ex.getMessage());
            return null;
        }
    }

    private boolean containsBookingIntent(String transcript) {
        if (!StringUtils.hasText(transcript)) {
            return false;
        }
        String normalized = transcript.toLowerCase();
        String plain = stripAccents(normalized);
        return INTENT_KEYWORDS.stream().anyMatch(keyword -> {
            String kwNorm = keyword.toLowerCase();
            String kwPlain = stripAccents(kwNorm);
            return containsWord(normalized, kwNorm) || containsWord(plain, kwPlain);
        });
    }

    private boolean containsWord(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return false;
        }
        String pattern = "(?<![\\p{L}\\p{N}])" + Pattern.quote(keyword) + "(?![\\p{L}\\p{N}])";
        return Pattern.compile(pattern, Pattern.UNICODE_CASE).matcher(text).find();
    }

    private String sanitizeTranscriptForBooking(String transcript) {
        return containsBookingIntent(transcript) ? transcript : "";
    }

    private String pickAppendableChunk(String raw, boolean hasIntent) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String sanitized = sanitizeTranscriptForBooking(raw);
        if (StringUtils.hasText(sanitized)) {
            return sanitized;
        }
        if (hasIntent && isLikelyBookingDetail(raw)) {
            return raw;
        }
        return "";
    }

    private boolean isLikelyBookingDetail(String raw) {
        if (!StringUtils.hasText(raw)) {
            return false;
        }
        String plain = stripAccents(raw.toLowerCase());
        if (plain.matches(".*\\d.*")) {
            return true;
        }
        if (plain.matches(".*\\b(gi(ó|o)|h|am|pm|sáng|ch(i|í)ều|t(ố|ô)i|mai|ng(à|a)y|th(ứ|u)|ca)\\b.*")) {
            return true;
        }
        List<String> detailKeywords = List.of(
                "dia chi", "địa chỉ", "diachi", "address", "phuong", "quan", "duong", "hcm", "ha noi",
                "tp", "tp.", "thanh pho", "khu", "can ho", "block", "toa", "apartment", "so ", "số "
        );
        for (String kw : detailKeywords) {
            String kwPlain = stripAccents(kw.toLowerCase());
            if (plain.contains(kwPlain)) {
                return true;
            }
        }
        return false;
    }

    private String buildClarificationText(List<String> missingFields, Map<String, Object> extractedInfo, String fallback) {
        if (missingFields == null) {
            return fallback;
        }

        boolean missingService = missingFields.contains("service");
        boolean missingCore = missingService
                && missingFields.contains("bookingTime")
                && missingFields.contains("address");

        boolean hasExtraInfo = extractedInfo != null && extractedInfo.entrySet().stream()
                .anyMatch(e -> {
                    String key = e.getKey();
                    if ("note".equalsIgnoreCase(key) || "availableServices".equalsIgnoreCase(key)) {
                        return false;
                    }
                    return e.getValue() != null && !"".equals(String.valueOf(e.getValue()).trim());
                });

        String availableServices = formatAvailableServices(extractedInfo != null ? extractedInfo.get("availableServices") : null);

        if (missingService && !hasExtraInfo) {
            return availableServices != null
                    ? MISSING_DETAIL_PROMPT + "\n" + availableServices
                    : MISSING_DETAIL_PROMPT;
        }
        if (missingCore && !hasExtraInfo) {
            return availableServices != null
                    ? MISSING_DETAIL_PROMPT + "\n" + availableServices
                    : MISSING_DETAIL_PROMPT;
        }
        String base = StringUtils.hasText(fallback) ? fallback : MISSING_DETAIL_PROMPT;
        if (availableServices != null && missingService) {
            return base + "\n" + availableServices;
        }
        return base;
    }

    private String formatAvailableServices(Object raw) {
        if (raw == null) {
            return null;
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty()) {
            return null;
        }
        return "Các dịch vụ hiện có: " + text;
    }

    private String stripAccents(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private String pickTtsMessage(String aiPrompt, String message, String clarification) {
        if (StringUtils.hasText(aiPrompt)) {
            return aiPrompt;
        }
        if (StringUtils.hasText(clarification)) {
            return clarification;
        }
        return message;
    }

    private List<String> defaultFailureHints() {
        return List.of(
                "Vui lòng kiểm tra micro và thử ghi âm lại.",
                "Đảm bảo đường truyền mạng ổn định.",
                "Thử nói rõ hơn về dịch vụ, thời gian và địa chỉ."
        );
    }

    private VoiceBookingResponse enrichFailure(VoiceBookingResponse response) {
        if (response == null) {
            return null;
        }
        return response.toBuilder()
                .failureHints(defaultFailureHints())
                .retryAfterMs(3000)
                .isFinal(true)
                .build();
    }

    private boolean allowAiPromptForMissing(List<String> missingFields) {
        if (missingFields == null || missingFields.isEmpty()) {
            return false;
        }
        if (missingFields.size() == 1 && missingFields.contains("service")) {
            return false; // chỉ thiếu dịch vụ thì dùng message hệ thống
        }
        // thiếu thời gian/địa chỉ hoặc nhiều hơn 1 trường -> cho phép AI gợi ý
        return missingFields.contains("bookingTime") || missingFields.contains("address") || missingFields.size() > 1;
    }

    private String buildAiInstruction(List<String> missingFields, Map<String, Object> extractedInfo) {
        if (missingFields == null || missingFields.isEmpty()) {
            return "";
        }
        List<String> display = new ArrayList<>();
        for (String f : missingFields) {
            display.add(formatFieldNameLocal(f));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Chỉ hỏi về các thông tin còn thiếu: ").append(String.join(", ", display)).append(". Không yêu cầu hay bịa đặt thêm thông tin khác.");
        if (missingFields.contains("service") && extractedInfo != null) {
            Object available = extractedInfo.get("availableServices");
            if (available != null) {
                sb.append(" Danh sách dịch vụ hợp lệ: ").append(String.valueOf(available));
            }
        }
        return sb.toString();
    }

    private String buildServiceAiInstruction(Map<String, Object> extractedInfo) {
        Object services = extractedInfo != null ? extractedInfo.get("availableServices") : null;
        String list = services != null ? String.valueOf(services) : "";
        return AI_SERVICE_INSTRUCTION + " Danh sách dịch vụ hợp lệ: " + list;
    }

    private String formatFieldNameLocal(String fieldName) {
        return switch (fieldName) {
            case "service", "services" -> "dịch vụ";
            case "bookingTime" -> "thời gian";
            case "address" -> "địa chỉ";
            case "note" -> "ghi chú";
            default -> fieldName;
        };
    }

    private boolean shouldUseAiPrompt(List<String> missingFields, Map<String, Object> extractedInfo) {
        if (missingFields == null || missingFields.isEmpty()) {
            return false;
        }
        // Always allow AI to build prompt when we have missing fields; upstream will decide instruction text.
        return true;
    }

    private record VoiceBookingDraftResult(BookingCreateRequest bookingRequest, VoiceBookingPreview preview) {
    }

    private record AutoAssignmentResult(
            BookingCreateRequest bookingRequest,
            List<VoiceBookingEmployeePreview> employeePreviews,
            boolean autoAssigned
    ) {
    }

    /**
     * Update voice request with transcript results
     */
    private void updateVoiceRequestWithTranscript(
            iuh.house_keeping_service_be.models.VoiceBookingRequest voiceRequest,
            VoiceToTextResult voiceResult
    ) {
        voiceRequest.setTranscript(voiceResult.transcript());
        voiceRequest.setProcessingTimeMs((int) voiceResult.processingTimeMs());

        if (voiceResult.confidenceScore() != null) {
            voiceRequest.setConfidenceScore(BigDecimal.valueOf(voiceResult.confidenceScore()));
        }

        voiceBookingRequestRepository.save(voiceRequest);
    }

    /**
     * Build a user-friendly error message for confirmation failures based on current status
     */
    private String buildConfirmationErrorMessage(String currentStatus) {
        return switch (currentStatus) {
            case "PARTIAL" -> "Yêu cầu đặt lịch chưa hoàn tất. Vui lòng cung cấp thêm thông tin còn thiếu trước khi xác nhận.";
            case "PROCESSING" -> "Yêu cầu đang được xử lý. Vui lòng đợi xử lý hoàn tất trước khi xác nhận.";
            case "PENDING" -> "Yêu cầu đang chờ xử lý. Vui lòng đợi hệ thống xử lý giọng nói.";
            case "COMPLETED" -> "Đơn đặt lịch này đã được xác nhận trước đó.";
            case "CANCELLED" -> "Yêu cầu đặt lịch này đã bị huỷ.";
            case "FAILED" -> "Yêu cầu đặt lịch này đã thất bại. Vui lòng tạo yêu cầu mới.";
            default -> "Trạng thái yêu cầu không hợp lệ: " + currentStatus + ". Không thể xác nhận.";
        };
    }
}
