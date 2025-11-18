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
import iuh.house_keeping_service_be.services.VoiceBookingService.VoiceBookingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import iuh.house_keeping_service_be.utils.BookingDTOFormatter;

import java.math.BigDecimal;
import java.util.*;
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
    private final EmployeeScheduleService employeeScheduleService;
    private final EmployeeRepository employeeRepository;

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
                log.info("Voice booking request {} parsed successfully, preparing preview", voiceRequest.getId());
                VoiceBookingDraftResult draftResult = prepareDraft(customer, parsedInfo.bookingRequest());
                voiceRequest.markAsAwaitingConfirmation(draftResult.bookingRequest(), draftResult.preview());
                voiceRequest = voiceBookingRequestRepository.save(voiceRequest);
                voiceBookingEventPublisher.emitAwaitingConfirmation(
                        voiceRequest.getId(),
                        username,
                        draftResult.preview(),
                        (int) voiceResult.processingTimeMs()
                );

                return VoiceBookingResponse.awaitingConfirmation(
                        voiceRequest.getId(),
                        voiceResult.transcript(),
                        draftResult.preview(),
                        voiceResult.confidenceScore(),
                        (int) voiceResult.processingTimeMs()
                );
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
        iuh.house_keeping_service_be.models.VoiceBookingRequest voiceRequest = voiceBookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Voice booking request not found: " + requestId));

        // Validate status
        if (!"PARTIAL".equals(voiceRequest.getStatus()) && !"AWAITING_CONFIRMATION".equals(voiceRequest.getStatus())) {
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
                log.info("Parsed update successfully for request {}, refreshing preview", requestId);
                VoiceBookingDraftResult draftResult = prepareDraft(customer, parsedInfo.bookingRequest());
                voiceRequest.markAsAwaitingConfirmation(draftResult.bookingRequest(), draftResult.preview());
                voiceRequest = voiceBookingRequestRepository.save(voiceRequest);
                voiceBookingEventPublisher.emitAwaitingConfirmation(
                        requestId,
                        username,
                        draftResult.preview(),
                        voiceRequest.getProcessingTimeMs()
                );

                return VoiceBookingResponse.awaitingConfirmation(
                        requestId,
                        combinedTranscript,
                        draftResult.preview(),
                        voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null,
                        voiceRequest.getProcessingTimeMs()
                );
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
     * Persist the pending draft into an actual booking once the customer confirms.
     */
    @Transactional
    public VoiceBookingResponse confirmVoiceBooking(String requestId, String username) {
        log.info("Confirming voice booking draft {}", requestId);

        iuh.house_keeping_service_be.models.VoiceBookingRequest voiceRequest = voiceBookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Voice booking request not found: " + requestId));

        if (!"AWAITING_CONFIRMATION".equals(voiceRequest.getStatus())) {
            throw new IllegalStateException("Voice booking request is not awaiting confirmation");
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
            voiceBookingEventPublisher.emitCompleted(
                    requestId,
                    username,
                    bookingSummary.getBookingId(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getProcessingTimeMs()
            );

            return VoiceBookingResponse.completed(
                    requestId,
                    bookingSummary.getBookingId(),
                    voiceRequest.getTranscript(),
                    voiceRequest.getConfidenceScore() != null ? voiceRequest.getConfidenceScore().doubleValue() : null,
                    voiceRequest.getProcessingTimeMs()
            );
        } catch (iuh.house_keeping_service_be.exceptions.BookingValidationException e) {
            log.error("Booking validation failed during confirmation {}: {}", requestId, e.getMessage());
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
                    "Không thể tạo booking: " + e.getMessage(),
                    e.getMessage()
            );
        } catch (Exception e) {
            log.error("Unexpected error confirming booking {}: {}", requestId, e.getMessage(), e);
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
                    "Đã xảy ra lỗi khi xác nhận đơn",
                    e.getMessage()
            );
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
        voiceBookingEventPublisher.emitCancelled(requestId, username);
        voiceBookingRequestRepository.delete(voiceRequest);

        return VoiceBookingResponse.cancelled(requestId);
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
                original.paymentMethodId()
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
}
