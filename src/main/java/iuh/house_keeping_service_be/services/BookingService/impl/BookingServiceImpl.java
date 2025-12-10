package iuh.house_keeping_service_be.services.BookingService.impl;

import iuh.house_keeping_service_be.config.CacheConfig;
import iuh.house_keeping_service_be.dtos.Booking.internal.*;
import iuh.house_keeping_service_be.dtos.Booking.request.*;
import iuh.house_keeping_service_be.dtos.Booking.response.*;
import iuh.house_keeping_service_be.dtos.Booking.summary.*;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.ApiResponse;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeRequest;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeResponse;
import iuh.house_keeping_service_be.dtos.Booking.BookingStatusWebSocketEvent;
import iuh.house_keeping_service_be.enums.AdditionalFeeType;
import iuh.house_keeping_service_be.dtos.Service.*;
import iuh.house_keeping_service_be.enums.*;
import iuh.house_keeping_service_be.exceptions.*;
import iuh.house_keeping_service_be.mappers.BookingMapper;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.EmployeeScheduleService.EmployeeScheduleService;
import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import iuh.house_keeping_service_be.services.ServiceService.ServiceService;
import iuh.house_keeping_service_be.services.WebSocketNotificationService.BookingRealtimeEventPublisher;
import iuh.house_keeping_service_be.services.AdditionalFeeService.AdditionalFeeService;
import iuh.house_keeping_service_be.utils.BookingDTOFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import iuh.house_keeping_service_be.dtos.Booking.response.FeeBreakdownResponse;
import iuh.house_keeping_service_be.models.AdditionalFee;
import iuh.house_keeping_service_be.dtos.Booking.internal.FeeBreakdownInfo;

/**
 * Optimized BookingService implementation with:
 * - Pre-validation and caching of shared data for multiple bookings
 * - Controlled parallel processing with thread pool
 * - Batch inserts for booking details and assignments
 * - Reduced logging overhead (DEBUG level for per-item logs)
 */

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    // Thread pool for parallel booking creation (small pool to control DB connections)
    private static final int BOOKING_THREAD_POOL_SIZE = 3;
    private final ExecutorService bookingExecutor = Executors.newFixedThreadPool(BOOKING_THREAD_POOL_SIZE);

    // Self-injection for @Transactional proxy calls
    @Lazy
    @Autowired
    private BookingServiceImpl self;

    // Repositories
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final AssignmentRepository assignmentRepository;
    private final AddressRepository addressRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final PromotionRepository promotionRepository;
    private final ServiceOptionChoiceRepository serviceOptionChoiceRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final RuleConditionRepository ruleConditionRepository;
    private final BookingRealtimeEventPublisher bookingRealtimeEventPublisher;
    private final BookingAdditionalFeeRepository bookingAdditionalFeeRepository;

    // Other Services
    private final ServiceService serviceService;
    private final BookingMapper bookingMapper;
    private final EmployeeScheduleService employeeScheduleService;
    private final NotificationService notificationService;
    private final AdditionalFeeService additionalFeeService;

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(
            value = CacheConfig.PROMOTION_VALIDATION_CACHE,
            allEntries = true,
            condition = "#request.promoCode() != null && !#request.promoCode().isEmpty()"
    )
    public BookingCreationSummary createBooking(BookingCreateRequest request) {
        log.info("Creating booking for customer with {} services", request.bookingDetails().size());

        try {
            // Step 1: Validate booking request (no need for clients to call validate endpoint first)
            ValidationOutcome validationOutcome = performValidation(request);
            BookingValidationResult validation = validationOutcome.result();
            if (!validation.isValid()) {
                if (validation.getErrors() != null && !validation.getErrors().isEmpty()) {
                    throw BookingValidationException.withErrors(validation.getErrors());
                }

                if (validation.getConflicts() != null && !validation.getConflicts().isEmpty()) {
                    throw EmployeeConflictException.withConflicts(validation.getConflicts());
                }

                throw BookingValidationException.withErrors(List.of("Booking validation failed"));
            }

            boolean hasAssignments = request.assignments() != null && !request.assignments().isEmpty();
            boolean hasTitle = request.title() != null && !request.title().trim().isEmpty();
            boolean hasImageUrls = request.imageUrls() != null && !request.imageUrls().isEmpty();

            // Auto-assign employee if assignments, title, and imageUrls are all empty
            BookingCreateRequest finalRequest = request;
            List<SuitableEmployeeResponse> selectedEmployees = new ArrayList<>();
            
            if (!hasAssignments && !hasTitle && !hasImageUrls) {
                log.info("No assignments, title, or imageUrls provided. Auto-assigning suitable employees...");
                
                // Get address information for finding suitable employees
                Address bookingAddress = validation.getAddress();
                if (bookingAddress == null) {
                    bookingAddress = addressRepository.findById(request.addressId())
                            .orElseThrow(() -> AddressNotFoundException.withId(request.addressId()));
                }
                
                String ward = bookingAddress.getWard();
                String city = bookingAddress.getCity();
                
                // Get customerId from validation context
                String customerId = validationOutcome.addressContext().customer() != null 
                    ? validationOutcome.addressContext().customer().getCustomerId() 
                    : null;
                
                // Find suitable employees for each service in the booking
                List<AssignmentRequest> autoAssignments = new ArrayList<>();
                
                for (BookingDetailRequest detailRequest : request.bookingDetails()) {
                    SuitableEmployeeRequest employeeRequest = 
                        new SuitableEmployeeRequest(
                            detailRequest.serviceId(),
                            request.bookingTime(),
                            ward,
                            city,
                            customerId,  // Truyền customerId để ưu tiên nhân viên đã từng phục vụ
                            null  // bookingTimes - không cần kiểm tra nhiều slot cho auto-assign
                        );
                    
                    ApiResponse<List<SuitableEmployeeResponse>> 
                        suitableEmployeesResponse = employeeScheduleService.findSuitableEmployees(employeeRequest);
                    
                    if (suitableEmployeesResponse.success() && 
                        suitableEmployeesResponse.data() != null && 
                        !suitableEmployeesResponse.data().isEmpty()) {
                        
                        // Select the first (best) employee from the list
                        SuitableEmployeeResponse selectedEmployee = 
                            suitableEmployeesResponse.data().get(0);
                        
                        selectedEmployees.add(selectedEmployee);
                        
                        // Create assignment for this employee
                        AssignmentRequest assignment = new AssignmentRequest(
                            selectedEmployee.employeeId(),
                            detailRequest.serviceId()
                        );
                        autoAssignments.add(assignment);
                        
                        log.info("Auto-assigned employee {} ({}) for service {}",
                            selectedEmployee.employeeId(),
                            selectedEmployee.fullName(),
                            detailRequest.serviceId());
                    } else {
                        log.warn("No suitable employees found for service {}", detailRequest.serviceId());
                    }
                }
                
                // Update request with auto-assigned employees if any were found
                if (!autoAssignments.isEmpty()) {
                    finalRequest = new BookingCreateRequest(
                        request.addressId(),
                        request.newAddress(),
                        request.bookingTime(),
                        request.note(),
                        request.title(),
                        request.imageUrls(),
                        request.promoCode(),
                        request.bookingDetails(),
                        autoAssignments,
                        request.paymentMethodId(),
                        request.additionalFeeIds()
                    );
                    hasAssignments = true;
                    log.info("Auto-assigned {} employees to booking", autoAssignments.size());
                }
            }

            // Step 2: Re-validate employee availability (double-check for conflicts) if assignments provided
            if (hasAssignments) {
                validateEmployeeAvailabilityFinal(finalRequest);
            }

            // Step 3: Create booking entity
            Booking booking = createBookingEntity(finalRequest, validation);

            if (!hasAssignments) {
                booking.setStatus(BookingStatus.AWAITING_EMPLOYEE);
            }

            // Set isVerified based on assignments
            // If no assignments (empty list), set isVerified = false (becomes a post needing admin approval)
            // If has assignments, set isVerified = true (normal booking)
            booking.setIsVerified(hasAssignments);

            // Set title and imageUrls from request if provided
            if (finalRequest.title() != null && !finalRequest.title().trim().isEmpty()) {
                booking.setTitle(finalRequest.title());
            }
            if (finalRequest.imageUrls() != null && !finalRequest.imageUrls().isEmpty()) {
                booking.setImageUrls(finalRequest.imageUrls());
            }

            log.info("Booking isVerified={}, hasAssignments={}, title={}, imageUrls={}, autoAssignedEmployees={}",
                    booking.getIsVerified(), hasAssignments, booking.getTitle(), booking.getImageUrls(), selectedEmployees.size());

            // Step 4: Create booking details
            List<BookingDetail> bookingDetails = createBookingDetails(booking, finalRequest, validation);
            // Step 4.1: Create additional fee snapshots
            List<BookingAdditionalFee> appliedFees = createBookingAdditionalFees(booking, validation);

            // Step 5: Create assignments if provided
            List<Assignment> assignments = hasAssignments
                    ? createAssignments(bookingDetails, finalRequest)
                    : Collections.emptyList();

            // Step 6: Create payment record
            Payment payment = createPaymentRecord(booking, finalRequest.paymentMethodId());

            // Step 7: Save all entities
            Booking savedBooking = bookingRepository.save(booking);
            List<BookingDetail> savedDetails = bookingDetailRepository.saveAll(bookingDetails);
            List<Assignment> savedAssignments = hasAssignments
                    ? assignmentRepository.saveAll(assignments)
                    : Collections.emptyList();
            Payment savedPayment = paymentRepository.save(payment);
            bookingAdditionalFeeRepository.saveAll(appliedFees);

            log.info("Booking created successfully with ID: {}", savedBooking.getBookingId());
            notifyCustomerBookingCreated(savedBooking);

            if (hasAssignments && !savedAssignments.isEmpty()) {
                notifyAssignedEmployees(savedAssignments, savedBooking);
            }

            // Step 8: Return creation summary with auto-assignment info
            boolean hasAutoAssignedEmployees = !selectedEmployees.isEmpty();
            return createBookingCreationSummary(savedBooking, savedDetails, savedAssignments, savedPayment, hasAutoAssignedEmployees);

        } catch (BookingValidationException | EmployeeConflictException e) {
            // Re-throw validation and conflict exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage(), e);
            throw BookingCreationException.withCause(e.getMessage(), e);
        }
    }

    @Override
    public BookingResponse getBookingDetails(String bookingId) {
        log.info("Fetching booking details for ID: {}", bookingId);

        Booking booking = bookingRepository.findBookingWithDetails(bookingId)
                .orElseThrow(() -> BookingNotFoundException.withId(bookingId));

        return bookingMapper.toResponse(booking);
    }

    @Override
    public BookingValidationResult validateBooking(BookingCreateRequest request) {
        log.info("Validating booking request");

        return performValidation(request).result();
    }

    @Override
    public BookingPreviewResponse previewBooking(BookingPreviewRequest request, String currentUserId, boolean isAdmin) {
        log.info("Generating booking preview for user: {}, isAdmin: {}", currentUserId, isAdmin);
        
        try {
            // Determine which customer to use
            String targetCustomerId = determineTargetCustomerId(request, currentUserId, isAdmin);
            
            // Convert preview request to internal format for validation
            PreviewValidationOutcome validationOutcome = performPreviewValidation(request, targetCustomerId);
            
            // Check for critical errors (customer/address not found)
            if (validationOutcome.hasCriticalError()) {
                return BookingPreviewResponse.error(validationOutcome.errors());
            }
            
            // Build the full preview response (includes any non-critical errors/warnings)
            return buildPreviewResponse(validationOutcome, request);
            
        } catch (Exception e) {
            log.error("Error generating booking preview: {}", e.getMessage(), e);
            return BookingPreviewResponse.error(List.of("Error generating preview: " + e.getMessage()));
        }
    }
    
    @Override
    public MultipleBookingPreviewResponse previewMultipleBookings(MultipleBookingPreviewRequest request, String currentUserId, boolean isAdmin) {
        log.info("Generating multiple booking preview for user: {}, isAdmin: {}, bookingTimes: {}", 
                currentUserId, isAdmin, request.bookingTimes().size());
        
        try {
            // Determine target customer
            String targetCustomerId = determineTargetCustomerIdForMultiple(request, currentUserId, isAdmin);
            
            List<BookingPreviewResponse> bookingPreviews = new ArrayList<>();
            List<LocalDateTime> validBookingTimes = new ArrayList<>();
            List<LocalDateTime> invalidBookingTimes = new ArrayList<>();
            List<String> allErrors = new ArrayList<>();
            BookingPreviewResponse firstValidPreview = null;
            
            // Process each booking time
            for (LocalDateTime bookingTime : request.bookingTimes()) {
                // Convert to single preview request
                BookingPreviewRequest singleRequest = new BookingPreviewRequest(
                        request.customerId(),
                        request.addressId(),
                        request.newAddress(),
                        bookingTime,
                        request.note(),
                        request.title(),
                        request.promoCode(), // Promo applies to ALL bookings
                        request.bookingDetails(),
                        request.paymentMethodId(),
                        request.additionalFeeIds()
                );
                
                PreviewValidationOutcome validationOutcome = performPreviewValidation(singleRequest, targetCustomerId);
                
                if (validationOutcome.hasCriticalError()) {
                    BookingPreviewResponse errorResponse = BookingPreviewResponse.error(validationOutcome.errors());
                    errorResponse.setBookingTime(bookingTime);
                    bookingPreviews.add(errorResponse);
                    invalidBookingTimes.add(bookingTime);
                    allErrors.addAll(validationOutcome.errors().stream()
                            .map(e -> bookingTime + ": " + e)
                            .toList());
                } else {
                    BookingPreviewResponse preview = buildPreviewResponse(validationOutcome, singleRequest);
                    bookingPreviews.add(preview);
                    if (preview.isValid()) {
                        validBookingTimes.add(bookingTime);
                        if (firstValidPreview == null) {
                            firstValidPreview = preview;
                        }
                    } else {
                        invalidBookingTimes.add(bookingTime);
                        allErrors.addAll(preview.getErrors().stream()
                                .map(e -> bookingTime + ": " + e)
                                .toList());
                    }
                }
            }
            
            // Calculate aggregated totals
            BigDecimal totalEstimatedPrice = bookingPreviews.stream()
                    .filter(BookingPreviewResponse::isValid)
                    .map(BookingPreviewResponse::getGrandTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate total duration
            int totalDurationMinutes = bookingPreviews.stream()
                    .filter(BookingPreviewResponse::isValid)
                    .map(BookingPreviewResponse::getEstimatedDuration)
                    .filter(Objects::nonNull)
                    .mapToInt(this::parseDurationToMinutes)
                    .sum();
            String totalDuration = formatMinutesToDuration(totalDurationMinutes);
            
            boolean allValid = invalidBookingTimes.isEmpty() && !validBookingTimes.isEmpty();
            
            // Build response with shared service info from first valid preview
            MultipleBookingPreviewResponse.MultipleBookingPreviewResponseBuilder responseBuilder = MultipleBookingPreviewResponse.builder()
                    .valid(allValid)
                    .errors(allValid ? List.of() : allErrors)
                    .bookingCount(request.bookingTimes().size())
                    .bookingPreviews(bookingPreviews)
                    .totalEstimatedPrice(totalEstimatedPrice)
                    .formattedTotalEstimatedPrice(BookingDTOFormatter.formatPrice(totalEstimatedPrice))
                    .totalEstimatedDuration(totalDuration)
                    .promoCode(request.promoCode())
                    .promoAppliedToAll(request.promoCode() != null && !request.promoCode().isBlank())
                    .validBookingsCount(validBookingTimes.size())
                    .invalidBookingsCount(invalidBookingTimes.size())
                    .validBookingTimes(validBookingTimes)
                    .invalidBookingTimes(invalidBookingTimes);
            
            // Add shared service info from first valid preview (same for all bookings)
            if (firstValidPreview != null) {
                responseBuilder
                        // Service items (shared)
                        .serviceItems(firstValidPreview.getServiceItems())
                        .totalServices(firstValidPreview.getTotalServices())
                        .totalQuantityPerBooking(firstValidPreview.getTotalQuantity())
                        .subtotalPerBooking(firstValidPreview.getSubtotal())
                        .formattedSubtotalPerBooking(firstValidPreview.getFormattedSubtotal())
                        // Customer info (shared)
                        .customerId(firstValidPreview.getCustomerId())
                        .customerName(firstValidPreview.getCustomerName())
                        .customerPhone(firstValidPreview.getCustomerPhone())
                        .customerEmail(firstValidPreview.getCustomerEmail())
                        .addressInfo(firstValidPreview.getAddressInfo())
                        .usingNewAddress(firstValidPreview.isUsingNewAddress())
                        // Payment method (shared)
                        .paymentMethodId(firstValidPreview.getPaymentMethodId())
                        .paymentMethodName(firstValidPreview.getPaymentMethodName())
                        // Fees (shared)
                        .feeBreakdowns(firstValidPreview.getFeeBreakdowns())
                        .totalFeesPerBooking(firstValidPreview.getTotalFees())
                        .formattedTotalFeesPerBooking(firstValidPreview.getFormattedTotalFees())
                        // Promotion (shared)
                        .promotionInfo(firstValidPreview.getPromotionInfo())
                        .discountPerBooking(firstValidPreview.getDiscountAmount())
                        .formattedDiscountPerBooking(firstValidPreview.getFormattedDiscountAmount())
                        // Price per booking
                        .pricePerBooking(firstValidPreview.getGrandTotal())
                        .formattedPricePerBooking(firstValidPreview.getFormattedGrandTotal())
                        // Duration per booking
                        .estimatedDurationPerBooking(firstValidPreview.getEstimatedDuration())
                        .recommendedStaff(firstValidPreview.getRecommendedStaff());
            }
            
            return responseBuilder.build();
            
        } catch (Exception e) {
            log.error("Error generating multiple booking preview: {}", e.getMessage(), e);
            return MultipleBookingPreviewResponse.error(List.of("Error generating preview: " + e.getMessage()));
        }
    }
    
    @Override
    public RecurringBookingPreviewResponse previewRecurringBooking(RecurringBookingPreviewRequest request, String currentUserId, boolean isAdmin) {
        log.info("Generating recurring booking preview for user: {}, isAdmin: {}, recurrenceType: {}", 
                currentUserId, isAdmin, request.recurrenceType());
        
        try {
            // Determine target customer
            String targetCustomerId = determineTargetCustomerIdForRecurring(request, currentUserId, isAdmin);
            
            // Normalize recurrence days
            List<Integer> normalizedDays = normalizeRecurrenceDays(request.recurrenceType(), request.recurrenceDays());
            
            // Calculate planned booking times (limited by maxPreviewOccurrences)
            int maxOccurrences = request.getEffectiveMaxPreviewOccurrences();
            List<LocalDateTime> allPlannedTimes = calculatePlannedBookingTimesForPreview(
                    request, normalizedDays, maxOccurrences + 1 // +1 to check if there are more
            );
            
            boolean hasMoreOccurrences = allPlannedTimes.size() > maxOccurrences;
            List<LocalDateTime> plannedTimes = allPlannedTimes.stream()
                    .limit(maxOccurrences)
                    .toList();
            
            if (plannedTimes.isEmpty()) {
                return RecurringBookingPreviewResponse.error(
                        List.of("Không có lịch đặt nào trong khoảng thời gian đã chọn")
                );
            }
            
            // Preview the first occurrence to get detailed pricing
            LocalDateTime firstBookingTime = plannedTimes.get(0);
            BookingPreviewRequest singleRequest = new BookingPreviewRequest(
                    request.customerId(),
                    request.addressId(),
                    request.newAddress(),
                    firstBookingTime,
                    request.note(),
                    request.title(),
                    request.promoCode(),
                    request.bookingDetails(),
                    request.paymentMethodId(),
                    request.additionalFeeIds()
            );
            
            PreviewValidationOutcome validationOutcome = performPreviewValidation(singleRequest, targetCustomerId);
            
            if (validationOutcome.hasCriticalError()) {
                return RecurringBookingPreviewResponse.error(validationOutcome.errors());
            }
            
            BookingPreviewResponse singlePreview = buildPreviewResponse(validationOutcome, singleRequest);
            
            // Calculate totals
            int occurrenceCount = plannedTimes.size();
            BigDecimal pricePerOccurrence = singlePreview.getGrandTotal() != null ? 
                    singlePreview.getGrandTotal() : BigDecimal.ZERO;
            BigDecimal totalEstimatedPrice = pricePerOccurrence.multiply(BigDecimal.valueOf(occurrenceCount));
            
            // Generate recurrence description
            String recurrenceDescription = RecurringBookingPreviewResponse.generateRecurrenceDescription(
                    request.recurrenceType(), normalizedDays, request.bookingTime()
            );
            
            return RecurringBookingPreviewResponse.builder()
                    .valid(singlePreview.isValid())
                    .errors(singlePreview.getErrors())
                    // Shared service info (same for all occurrences)
                    .serviceItems(singlePreview.getServiceItems())
                    .totalServices(singlePreview.getTotalServices())
                    .totalQuantityPerOccurrence(singlePreview.getTotalQuantity())
                    .subtotalPerOccurrence(singlePreview.getSubtotal())
                    .formattedSubtotalPerOccurrence(singlePreview.getFormattedSubtotal())
                    // Fees (shared)
                    .feeBreakdowns(singlePreview.getFeeBreakdowns())
                    .totalFeesPerOccurrence(singlePreview.getTotalFees())
                    .formattedTotalFeesPerOccurrence(singlePreview.getFormattedTotalFees())
                    // Discount per occurrence
                    .discountPerOccurrence(singlePreview.getDiscountAmount())
                    .formattedDiscountPerOccurrence(singlePreview.getFormattedDiscountAmount())
                    // Recurrence info
                    .recurrenceType(request.recurrenceType())
                    .recurrenceDays(normalizedDays)
                    .recurrenceDescription(recurrenceDescription)
                    .bookingTime(request.bookingTime())
                    .startDate(request.startDate())
                    .endDate(request.endDate())
                    .plannedBookingTimes(plannedTimes)
                    .occurrenceCount(occurrenceCount)
                    .maxPreviewOccurrences(maxOccurrences)
                    .hasMoreOccurrences(hasMoreOccurrences)
                    .singleBookingPreview(singlePreview)
                    .pricePerOccurrence(pricePerOccurrence)
                    .formattedPricePerOccurrence(BookingDTOFormatter.formatPrice(pricePerOccurrence))
                    .totalEstimatedPrice(totalEstimatedPrice)
                    .formattedTotalEstimatedPrice(BookingDTOFormatter.formatPrice(totalEstimatedPrice))
                    .estimatedDurationPerOccurrence(singlePreview.getEstimatedDuration())
                    .recommendedStaff(singlePreview.getRecommendedStaff())
                    // Customer and address info
                    .customerId(singlePreview.getCustomerId())
                    .customerName(singlePreview.getCustomerName())
                    .customerPhone(singlePreview.getCustomerPhone())
                    .customerEmail(singlePreview.getCustomerEmail())
                    .addressInfo(singlePreview.getAddressInfo())
                    .usingNewAddress(singlePreview.isUsingNewAddress())
                    // Payment method
                    .paymentMethodId(singlePreview.getPaymentMethodId())
                    .paymentMethodName(singlePreview.getPaymentMethodName())
                    // Promo info
                    .promoCode(request.promoCode())
                    .promoAppliedToAll(request.promoCode() != null && !request.promoCode().isBlank())
                    .promotionInfo(singlePreview.getPromotionInfo())
                    .build();
            
        } catch (Exception e) {
            log.error("Error generating recurring booking preview: {}", e.getMessage(), e);
            return RecurringBookingPreviewResponse.error(List.of("Error generating preview: " + e.getMessage()));
        }
    }
    
    private String determineTargetCustomerIdForMultiple(MultipleBookingPreviewRequest request, String currentUserId, boolean isAdmin) {
        if (isAdmin && request.customerId() != null && !request.customerId().isBlank()) {
            return request.customerId();
        }
        return currentUserId;
    }
    
    private String determineTargetCustomerIdForRecurring(RecurringBookingPreviewRequest request, String currentUserId, boolean isAdmin) {
        if (isAdmin && request.customerId() != null && !request.customerId().isBlank()) {
            return request.customerId();
        }
        return currentUserId;
    }
    
    /**
     * Calculate planned booking times for preview based on recurrence pattern.
     * Similar to RecurringBookingServiceImpl but simplified for preview purposes.
     */
    private List<LocalDateTime> calculatePlannedBookingTimesForPreview(
            RecurringBookingPreviewRequest request,
            List<Integer> normalizedRecurrenceDays,
            int maxOccurrences
    ) {
        List<LocalDateTime> times = new ArrayList<>();
        
        java.time.LocalDate currentDate = request.startDate();
        java.time.LocalDate effectiveEndDate = request.endDate();
        
        // If no end date, calculate up to 1 year from start
        if (effectiveEndDate == null) {
            effectiveEndDate = request.startDate().plusYears(1);
        }
        
        while (!currentDate.isAfter(effectiveEndDate) && times.size() < maxOccurrences) {
            if (shouldGenerateBookingForDatePreview(request.recurrenceType(), normalizedRecurrenceDays, currentDate)) {
                LocalDateTime bookingDateTime = LocalDateTime.of(currentDate, request.bookingTime());
                // Only include future times
                if (bookingDateTime.isAfter(LocalDateTime.now())) {
                    times.add(bookingDateTime);
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return times.stream().sorted().toList();
    }
    
    /**
     * Check if a booking should be generated for a specific date based on recurrence pattern.
     */
    private boolean shouldGenerateBookingForDatePreview(
            RecurrenceType type,
            List<Integer> recurrenceDays,
            java.time.LocalDate date
    ) {
        if (type == RecurrenceType.WEEKLY) {
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
            return recurrenceDays.contains(dayOfWeek);
        } else if (type == RecurrenceType.MONTHLY) {
            int dayOfMonth = date.getDayOfMonth();
            return recurrenceDays.contains(dayOfMonth);
        }
        return false;
    }
    
    /**
     * Normalize recurrence days based on recurrence type.
     * For WEEKLY: ensure values are 1-7 (Mon-Sun)
     * For MONTHLY: ensure values are 1-31
     */
    private List<Integer> normalizeRecurrenceDays(RecurrenceType type, List<Integer> days) {
        if (days == null || days.isEmpty()) {
            return List.of();
        }
        
        if (type == RecurrenceType.WEEKLY) {
            return days.stream()
                    .filter(d -> d >= 1 && d <= 7)
                    .distinct()
                    .sorted()
                    .toList();
        } else if (type == RecurrenceType.MONTHLY) {
            return days.stream()
                    .filter(d -> d >= 1 && d <= 31)
                    .distinct()
                    .sorted()
                    .toList();
        }
        return days;
    }
    
    /**
     * Parse duration string (e.g., "2 giờ 30 phút") to minutes.
     */
    private int parseDurationToMinutes(String duration) {
        if (duration == null || duration.isBlank()) {
            return 0;
        }
        
        int totalMinutes = 0;
        
        // Parse hours
        if (duration.contains("giờ")) {
            String[] parts = duration.split("giờ");
            try {
                totalMinutes += Integer.parseInt(parts[0].trim()) * 60;
            } catch (NumberFormatException ignored) {}
            
            // Parse remaining minutes if exists
            if (parts.length > 1 && parts[1].contains("phút")) {
                String minutePart = parts[1].replace("phút", "").trim();
                try {
                    totalMinutes += Integer.parseInt(minutePart);
                } catch (NumberFormatException ignored) {}
            }
        } else if (duration.contains("phút")) {
            String minutePart = duration.replace("phút", "").trim();
            try {
                totalMinutes = Integer.parseInt(minutePart);
            } catch (NumberFormatException ignored) {}
        }
        
        return totalMinutes;
    }
    
    /**
     * Format minutes to duration string (e.g., 150 -> "2 giờ 30 phút").
     */
    private String formatMinutesToDuration(int totalMinutes) {
        if (totalMinutes <= 0) {
            return "0 phút";
        }
        
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" giờ");
            if (minutes > 0) {
                sb.append(" ").append(minutes).append(" phút");
            }
        } else {
            sb.append(minutes).append(" phút");
        }
        
        return sb.toString();
    }
    
    private String determineTargetCustomerId(BookingPreviewRequest request, String currentUserId, boolean isAdmin) {
        // Admin can specify customerId, otherwise use current user
        if (isAdmin && request.customerId() != null && !request.customerId().isBlank()) {
            return request.customerId();
        }
        return currentUserId;
    }
    
    private PreviewValidationOutcome performPreviewValidation(BookingPreviewRequest request, String customerId) {
        log.info("Performing preview validation for customer: {}", customerId);
        
        List<String> errors = new ArrayList<>();      // Critical errors
        List<String> warnings = new ArrayList<>();    // Non-critical warnings (promotion issues, etc.)
        
        // Validate customer and address
        CustomerAddressContext addressContext;
        try {
            addressContext = validateCustomerAndAddressForPreview(request, customerId);
        } catch (AddressNotFoundException | CustomerNotFoundException | IllegalArgumentException e) {
            log.error("Error validating preview address: {}", e.getMessage());
            errors.add(e.getMessage());
            return new PreviewValidationOutcome(errors, warnings, null, null, null, null, null, null, null, null, null, null, null);
        } catch (Exception e) {
            log.error("Unexpected error validating preview address: {}", e.getMessage(), e);
            errors.add("Validation error: " + e.getMessage());
            return new PreviewValidationOutcome(errors, warnings, null, null, null, null, null, null, null, null, null, null, null);
        }
        
        Customer customer = addressContext.customer();
        
        // Validate services and calculate prices (skip booking time validation for preview)
        List<ServiceValidationInfo> serviceValidations = validateServices(request.bookingDetails(), errors);
        BigDecimal calculatedTotalAmount = serviceValidations.stream()
                .map(ServiceValidationInfo::getCalculatedPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Apply promotion with full result (cached)
        PromotionApplicationResult promotionResult = applyPromotionWithDetails(
                request.promoCode(), calculatedTotalAmount, customer);
        
        // Promotion errors are warnings, not critical errors
        if (promotionResult.hasError()) {
            warnings.add(promotionResult.errorMessage());
        }
        
        BigDecimal finalAmount = promotionResult.finalAmount();
        
        // Apply system surcharge + other fees
        FeeCalculationResult feeResult = calculateFees(finalAmount, request.additionalFeeIds());
        BigDecimal totalWithFees = finalAmount.add(feeResult.totalFees());
        
        // Get payment method info if provided
        iuh.house_keeping_service_be.models.PaymentMethod paymentMethod = null;
        if (request.paymentMethodId() > 0) {
            paymentMethod = paymentMethodRepository.findById(request.paymentMethodId()).orElse(null);
        }
        
        // Build choice details for display
        Map<Integer, List<ChoicePreviewItem>> choicesByService = buildChoicePreviewItems(request.bookingDetails());
        
        return new PreviewValidationOutcome(
                errors,
                warnings,
                addressContext,
                customer,
                serviceValidations,
                calculatedTotalAmount,
                promotionResult,
                finalAmount,
                feeResult,
                totalWithFees,
                paymentMethod,
                choicesByService,
                request.bookingTime()
        );
    }
    
    private CustomerAddressContext validateCustomerAndAddressForPreview(BookingPreviewRequest request, String customerId) {
        boolean hasAddressId = request.addressId() != null && !request.addressId().isBlank();
        boolean hasNewAddress = request.newAddress() != null;

        if ((hasAddressId && hasNewAddress) || (!hasAddressId && !hasNewAddress)) {
            throw new IllegalArgumentException("Either addressId or newAddress must be provided");
        }

        if (hasAddressId) {
            Address address = addressRepository.findAddressWithCustomer(request.addressId())
                    .orElseThrow(() -> AddressNotFoundException.withId(request.addressId()));

            Customer customer = address.getCustomer();
            if (customer == null) {
                throw CustomerNotFoundException.forAddress(request.addressId());
            }

            return new CustomerAddressContext(customer, address, false);
        }

        NewAddressRequest newAddress = request.newAddress();
        // For preview, use the customerId from the request or the determined target customer
        String targetCustomerId = newAddress.customerId() != null ? newAddress.customerId() : customerId;
        Customer customer = customerRepository.findById(targetCustomerId)
                .orElseThrow(() -> CustomerNotFoundException.withId(targetCustomerId));

        Address address = new Address();
        address.setCustomer(customer);
        address.setFullAddress(newAddress.fullAddress());
        address.setWard(newAddress.ward());
        address.setCity(newAddress.city());
        address.setLatitude(newAddress.latitude() != null ? BigDecimal.valueOf(newAddress.latitude()) : null);
        address.setLongitude(newAddress.longitude() != null ? BigDecimal.valueOf(newAddress.longitude()) : null);
        address.setIsDefault(Boolean.FALSE);

        return new CustomerAddressContext(customer, address, true);
    }
    
    /**
     * Apply promotion with full details for preview (cacheable).
     * Unlike applyPromotion(), this method returns all promotion details needed for display.
     */
    @org.springframework.cache.annotation.Cacheable(
            value = CacheConfig.PROMOTION_VALIDATION_CACHE,
            key = "#promoCode + ':' + #amount.toString() + ':' + #customer.customerId",
            unless = "#result.hasError()"
    )
    public PromotionApplicationResult applyPromotionWithDetails(String promoCode, BigDecimal amount, Customer customer) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return PromotionApplicationResult.noPromotion(amount);
        }

        Optional<Promotion> promotionOpt = promotionRepository.findAvailablePromotion(promoCode, LocalDateTime.now());
        if (promotionOpt.isEmpty()) {
            return PromotionApplicationResult.error(amount, "Promotion code is invalid or expired: " + promoCode);
        }

        Promotion promotion = promotionOpt.get();

        // Check customer usage limit (if applicable)
        long customerUsage = promotionRepository.countPromoCodeUsageByCustomer(promoCode, customer.getCustomerId());
        if (customerUsage > 0) { // Assuming one-time use per customer
            return PromotionApplicationResult.error(amount, "Promotion code has already been used by this customer");
        }

        // Calculate discount
        BigDecimal discount = BigDecimal.ZERO;
        if (promotion.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = promotion.getDiscountValue();
        } else if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = amount.multiply(promotion.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            // Apply max discount limit
            if (promotion.getMaxDiscountAmount() != null &&
                    discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                discount = promotion.getMaxDiscountAmount();
            }
        }

        BigDecimal finalAmount = amount.subtract(discount).max(BigDecimal.ZERO);
        
        // Build promotion info for display
        PromotionInfo promotionInfo = new PromotionInfo(
                promotion.getPromotionId(),
                promotion.getPromoCode(),
                promotion.getDescription(),
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                promotion.getMaxDiscountAmount()
        );

        return PromotionApplicationResult.success(finalAmount, discount, promotion, promotionInfo);
    }
    
    private Map<Integer, List<ChoicePreviewItem>> buildChoicePreviewItems(List<BookingDetailRequest> bookingDetails) {
        // Collect all choice IDs across all booking details
        List<Integer> allChoiceIds = bookingDetails.stream()
                .filter(detail -> detail.selectedChoiceIds() != null)
                .flatMap(detail -> detail.selectedChoiceIds().stream())
                .distinct()
                .collect(Collectors.toList());
        
        if (allChoiceIds.isEmpty()) {
            return Map.of();
        }
        
        // Fetch all choices with option names in a single query
        List<ServiceOptionChoice> choices = serviceOptionChoiceRepository.findChoicesWithOptionNames(allChoiceIds);
        
        // Build a map of choiceId -> ChoicePreviewItem with price from PricingRule
        Map<Integer, ChoicePreviewItem> choiceItemMap = choices.stream()
                .collect(Collectors.toMap(
                        ServiceOptionChoice::getId,
                        choice -> {
                            // Get price adjustment from PricingRule via RuleCondition
                            BigDecimal priceAdjustment = BigDecimal.ZERO;
                            try {
                                RuleCondition ruleCondition = ruleConditionRepository.findByChoice_Id(choice.getId());
                                if (ruleCondition != null && ruleCondition.getRule() != null 
                                        && ruleCondition.getRule().getPriceAdjustment() != null) {
                                    priceAdjustment = ruleCondition.getRule().getPriceAdjustment();
                                }
                            } catch (Exception e) {
                                log.debug("No pricing rule found for choice {}", choice.getId());
                            }
                            
                            return new ChoicePreviewItem(
                                    choice.getId(),
                                    choice.getLabel(),
                                    choice.getOption() != null ? choice.getOption().getLabel() : null,
                                    priceAdjustment,
                                    BookingDTOFormatter.formatPrice(priceAdjustment)
                            );
                        }
                ));
        
        // Group by service
        Map<Integer, List<ChoicePreviewItem>> result = new HashMap<>();
        for (BookingDetailRequest detail : bookingDetails) {
            if (detail.selectedChoiceIds() != null && !detail.selectedChoiceIds().isEmpty()) {
                List<ChoicePreviewItem> items = detail.selectedChoiceIds().stream()
                        .map(choiceItemMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                result.put(detail.serviceId(), items);
            }
        }
        
        return result;
    }
    
    private BookingPreviewResponse buildPreviewResponse(PreviewValidationOutcome outcome, BookingPreviewRequest request) {
        Customer customer = outcome.customer();
        CustomerAddressContext addressContext = outcome.addressContext();
        
        // Build service preview items
        List<ServicePreviewItem> serviceItems = buildServicePreviewItems(
                outcome.serviceValidations(),
                request.bookingDetails(),
                outcome.choicesByService()
        );
        
        // Calculate totals
        int totalServices = serviceItems.size();
        int totalQuantity = request.bookingDetails().stream()
                .mapToInt(d -> d.quantity() != null ? d.quantity() : 1)
                .sum();
        
        // Estimated duration (sum of all service durations)
        BigDecimal totalDurationHours = outcome.serviceValidations().stream()
                .filter(ServiceValidationInfo::isValid)
                .map(info -> {
                    Optional<Service> serviceOpt = serviceRepository.findById(info.getServiceId());
                    return serviceOpt.map(s -> s.getEstimatedDurationHours() != null ? 
                            s.getEstimatedDurationHours() : BigDecimal.ZERO).orElse(BigDecimal.ZERO);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Recommended staff (max of all services)
        int recommendedStaff = outcome.serviceValidations().stream()
                .filter(ServiceValidationInfo::isValid)
                .mapToInt(info -> info.getRecommendedStaff() != null ? info.getRecommendedStaff() : 1)
                .max()
                .orElse(1);
        
        // Build fee breakdowns
        List<FeeBreakdownResponse> feeBreakdowns = outcome.feeResult().breakdowns().stream()
                .map(info -> FeeBreakdownResponse.builder()
                        .name(info.getName())
                        .type(info.getType())
                        .value(info.getValue())
                        .amount(info.getAmount())
                        .systemSurcharge(info.isSystemSurcharge())
                        .build())
                .collect(Collectors.toList());
        
        PromotionApplicationResult promotionResult = outcome.promotionResult();
        
        // Get phone from customer's account
        String customerPhone = customer.getAccount() != null ? customer.getAccount().getPhoneNumber() : null;
        
        // Combine errors and warnings for display
        List<String> allMessages = outcome.allMessages();
        
        // valid = true only if no errors AND no warnings
        boolean isValid = outcome.errors().isEmpty() && outcome.warnings().isEmpty();
        
        return BookingPreviewResponse.builder()
                .valid(isValid)
                .errors(allMessages)  // Contains both errors and warnings
                // Customer info
                .customerId(customer.getCustomerId())
                .customerName(customer.getFullName())
                .customerPhone(customerPhone)
                .customerEmail(customer.getEmail())
                // Address info
                .addressInfo(buildAddressInfo(addressContext.address()))
                .usingNewAddress(addressContext.isNewAddress())
                // Booking time
                .bookingTime(outcome.bookingTime())
                // Service items
                .serviceItems(serviceItems)
                .totalServices(totalServices)
                .totalQuantity(totalQuantity)
                // Pricing
                .subtotal(outcome.calculatedTotalAmount())
                .formattedSubtotal(BookingDTOFormatter.formatPrice(outcome.calculatedTotalAmount()))
                // Promotion
                .promotionInfo(promotionResult.promotionInfo())
                .discountAmount(promotionResult.discountAmount())
                .formattedDiscountAmount(BookingDTOFormatter.formatPrice(promotionResult.discountAmount()))
                // After discount
                .totalAfterDiscount(outcome.finalAmount())
                .formattedTotalAfterDiscount(BookingDTOFormatter.formatPrice(outcome.finalAmount()))
                // Fees
                .feeBreakdowns(feeBreakdowns)
                .totalFees(outcome.feeResult().totalFees())
                .formattedTotalFees(BookingDTOFormatter.formatPrice(outcome.feeResult().totalFees()))
                // Grand total
                .grandTotal(outcome.totalWithFees())
                .formattedGrandTotal(BookingDTOFormatter.formatPrice(outcome.totalWithFees()))
                // Additional info
                .estimatedDuration(formatDuration(totalDurationHours))
                .recommendedStaff(recommendedStaff)
                .note(request.note())
                // Payment method
                .paymentMethodId(outcome.paymentMethod() != null ? outcome.paymentMethod().getMethodId() : null)
                .paymentMethodName(outcome.paymentMethod() != null ? outcome.paymentMethod().getMethodName() : null)
                .build();
    }
    
    private List<ServicePreviewItem> buildServicePreviewItems(
            List<ServiceValidationInfo> serviceValidations,
            List<BookingDetailRequest> bookingDetails,
            Map<Integer, List<ChoicePreviewItem>> choicesByService) {
        
        // Map service validations by serviceId
        Map<Integer, ServiceValidationInfo> validationMap = serviceValidations.stream()
                .collect(Collectors.toMap(ServiceValidationInfo::getServiceId, v -> v, (a, b) -> a));
        
        return bookingDetails.stream()
                .map(detail -> {
                    ServiceValidationInfo validation = validationMap.get(detail.serviceId());
                    if (validation == null || !validation.isValid()) {
                        return null;
                    }
                    
                    // Get full service details
                    Optional<Service> serviceOpt = serviceRepository.findById(detail.serviceId());
                    Service service = serviceOpt.orElse(null);
                    
                    int quantity = detail.quantity() != null ? detail.quantity() : 1;
                    BigDecimal unitPrice = validation.getCalculatedPrice()
                            .divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
                    
                    return new ServicePreviewItem(
                            detail.serviceId(),
                            validation.getServiceName(),
                            service != null ? service.getDescription() : null,
                            service != null ? service.getIconUrl() : null,
                            quantity,
                            service != null ? service.getUnit() : null,
                            unitPrice,
                            BookingDTOFormatter.formatPrice(unitPrice),
                            validation.getCalculatedPrice(),
                            BookingDTOFormatter.formatPrice(validation.getCalculatedPrice()),
                            choicesByService.getOrDefault(detail.serviceId(), List.of()),
                            service != null ? formatDuration(service.getEstimatedDurationHours()) : null,
                            validation.getRecommendedStaff()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private CustomerAddressInfo buildAddressInfo(Address address) {
        if (address == null) return null;
        
        return new CustomerAddressInfo(
                address.getAddressId(),
                address.getFullAddress(),
                address.getWard(),
                address.getCity(),
                address.getLatitude() != null ? address.getLatitude().doubleValue() : null,
                address.getLongitude() != null ? address.getLongitude().doubleValue() : null,
                address.getIsDefault()
        );
    }
    
    private String formatDuration(BigDecimal hours) {
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        
        int totalMinutes = hours.multiply(BigDecimal.valueOf(60)).intValue();
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        
        if (h > 0 && m > 0) {
            return h + " giờ " + m + " phút";
        } else if (h > 0) {
            return h + " giờ";
        } else {
            return m + " phút";
        }
    }
    
    /**
     * Preview validation outcome containing all calculated values for building the response.
     */
    private record PreviewValidationOutcome(
            List<String> errors,
            List<String> warnings,
            CustomerAddressContext addressContext,
            Customer customer,
            List<ServiceValidationInfo> serviceValidations,
            BigDecimal calculatedTotalAmount,
            PromotionApplicationResult promotionResult,
            BigDecimal finalAmount,
            FeeCalculationResult feeResult,
            BigDecimal totalWithFees,
            iuh.house_keeping_service_be.models.PaymentMethod paymentMethod,
            Map<Integer, List<ChoicePreviewItem>> choicesByService,
            LocalDateTime bookingTime
    ) {
        /**
         * Check if there are critical errors that prevent building a full response.
         * Critical errors: customer not found, address not found, no valid services.
         */
        public boolean hasCriticalError() {
            return customer == null || addressContext == null;
        }
        
        /**
         * Get all messages (errors + warnings) for display.
         */
        public List<String> allMessages() {
            List<String> all = new ArrayList<>(errors);
            all.addAll(warnings);
            return all;
        }
    }

    private ValidationOutcome performValidation(BookingCreateRequest request) {
        log.info("Performing booking validation");

        List<String> errors = new ArrayList<>();
        List<ConflictInfo> conflicts = new ArrayList<>();

        CustomerAddressContext addressContext;

        try {
            addressContext = validateCustomerAndAddress(request);
        } catch (AddressNotFoundException | CustomerNotFoundException | IllegalArgumentException e) {
            log.error("Error validating booking address: {}", e.getMessage());
            errors.add(e.getMessage());
            return new ValidationOutcome(BookingValidationResult.error(errors), null);
        } catch (Exception e) {
            log.error("Unexpected error validating booking address: {}", e.getMessage(), e);
            errors.add("Validation error: " + e.getMessage());
            return new ValidationOutcome(BookingValidationResult.error(errors), null);
        }

        try {
            Customer customer = addressContext.customer();

            // Validate booking time
            validateBookingTime(request.bookingTime(), errors);

            // Validate services and calculate prices
            List<ServiceValidationInfo> serviceValidations = validateServices(request.bookingDetails(), errors);
            BigDecimal calculatedTotalAmount = serviceValidations.stream()
                    .map(ServiceValidationInfo::getCalculatedPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Apply promotion if provided
            BigDecimal finalAmount = applyPromotion(request.promoCode(), calculatedTotalAmount, customer, errors);

            // Apply system surcharge + other fees
            FeeCalculationResult feeResult = calculateFees(finalAmount, request.additionalFeeIds());
            BigDecimal totalWithFees = finalAmount.add(feeResult.totalFees());

            if (!errors.isEmpty()) {
                return new ValidationOutcome(BookingValidationResult.error(errors), addressContext);
            }

            if (request.assignments() != null && !request.assignments().isEmpty()) {
                int requiredEmployees = calculateRequiredEmployees(request.bookingDetails(), serviceValidations);
                long assignedEmployees = request.assignments().stream()
                        .map(AssignmentRequest::employeeId)
                        .distinct()
                        .count();
                if (requiredEmployees != assignedEmployees) {
                    errors.add("Total employees assigned (" + assignedEmployees + ") does not match required employees (" + requiredEmployees + ")");
                }

                if (!errors.isEmpty()) {
                    return new ValidationOutcome(BookingValidationResult.error(errors), addressContext);
                }

                // Validate employee assignments
                validateEmployeeAssignments(request.assignments(), request.bookingTime(), conflicts);

                if (!conflicts.isEmpty()) {
                    return new ValidationOutcome(BookingValidationResult.conflict(conflicts), addressContext);
                }
            }

            BookingValidationResult successResult = BookingValidationResult.success(
                    totalWithFees,
                    finalAmount,
                    feeResult.totalFees(),
                    feeResult.breakdowns(),
                    serviceValidations,
                    customer,
                    addressContext.address(),
                    addressContext.isNewAddress());
            return new ValidationOutcome(successResult, addressContext);

        } catch (Exception e) {
            log.error("Error during booking validation: {}", e.getMessage(), e);
            errors.add("Validation error: " + e.getMessage());
            return new ValidationOutcome(BookingValidationResult.error(errors), addressContext);
        }
    }

    // Private helper methods

    private CustomerAddressContext validateCustomerAndAddress(BookingCreateRequest request) {
        boolean hasAddressId = request.addressId() != null && !request.addressId().isBlank();
        boolean hasNewAddress = request.newAddress() != null;

        if ((hasAddressId && hasNewAddress) || (!hasAddressId && !hasNewAddress)) {
            throw new IllegalArgumentException("Either addressId or newAddress must be provided");
        }

        if (hasAddressId) {
            Address address = addressRepository.findAddressWithCustomer(request.addressId())
                    .orElseThrow(() -> AddressNotFoundException.withId(request.addressId()));


            Customer customer = address.getCustomer();
            if (customer == null) {
                throw CustomerNotFoundException.forAddress(request.addressId());
            }

            return new CustomerAddressContext(customer, address, false);
        }

        NewAddressRequest newAddress = request.newAddress();
        Customer customer = customerRepository.findById(newAddress.customerId())
                .orElseThrow(() -> CustomerNotFoundException.withId(newAddress.customerId()));

        Address address = new Address();
        address.setCustomer(customer);
        address.setFullAddress(newAddress.fullAddress());
        address.setWard(newAddress.ward());
        address.setCity(newAddress.city());
        address.setLatitude(newAddress.latitude() != null ? BigDecimal.valueOf(newAddress.latitude()) : null);
        address.setLongitude(newAddress.longitude() != null ? BigDecimal.valueOf(newAddress.longitude()) : null);
        address.setIsDefault(Boolean.FALSE);

        return new CustomerAddressContext(customer, address, true);
    }

    private void validateBookingTime(LocalDateTime bookingTime, List<String> errors) {
        if (bookingTime == null) {
            errors.add("Booking time is required");
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();

        // Check if booking time is in the future
        // Skip the "30 minutes from now" check for recurring bookings (bookings scheduled more than 1 day ahead)
        // This allows recurring bookings to be generated for future dates
        if (bookingTime.isBefore(now)) {
            errors.add("Booking time cannot be in the past");
        } else if (bookingTime.isBefore(now.plusDays(1)) && bookingTime.isBefore(now.plusMinutes(30))) {
            // Only apply the 2-hour rule for bookings within the next 24 hours (manual bookings)
            errors.add("Booking time must be at least 30 minutes from now");
        }

        // Check if booking time is within business hours (8 AM - 8 PM)
        int hour = bookingTime.getHour();
        if (hour < 8 || hour > 20) {
            errors.add("Booking time must be between 8:00 AM and 8:00 PM");
        }


    }

    private List<ServiceValidationInfo> validateServices(List<BookingDetailRequest> detailRequests, List<String> errors) {
        return detailRequests.stream()
                .map(detail -> validateSingleService(detail, errors))
                .collect(Collectors.toList());
    }

    private ServiceValidationInfo validateSingleService(BookingDetailRequest detail, List<String> errors) {
        Optional<Service> serviceOpt =
                serviceRepository.findBookableService(detail.serviceId());

        if (serviceOpt.isEmpty()) {
            errors.add("Service not found or not bookable: " + detail.serviceId());
            return ServiceValidationInfo.invalid(detail.serviceId(), "Service not found or not bookable");
        }

        var service = serviceOpt.get();

        // Validate choice IDs
        List<Integer> validChoiceIds = List.of();
        List<Integer> invalidChoiceIds = List.of();

        if (detail.selectedChoiceIds() != null && !detail.selectedChoiceIds().isEmpty()) {
            List<Integer> foundChoiceIds = serviceOptionChoiceRepository
                    .validateChoiceIdsForService(detail.serviceId(), detail.selectedChoiceIds());

            validChoiceIds = new ArrayList<>(foundChoiceIds);
            invalidChoiceIds = detail.selectedChoiceIds().stream()
                    .filter(id -> !foundChoiceIds.contains(id))
                    .toList();

            if (!invalidChoiceIds.isEmpty()) {
                errors.add("Invalid choice IDs for service " + detail.serviceId() + ": " + invalidChoiceIds);
            }
        }

        // Calculate actual price using ServiceService
        ServicePricingResult pricingResult = calculateServicePricing(detail, service, validChoiceIds);
        BigDecimal calculatedPrice = pricingResult.totalPrice();

        // Compare with expected price (tolerance of 1000 VND) - only if expectedPrice is provided
        boolean priceMatches = true; // Default to true if no expectedPrice provided
        if (detail.expectedPrice() != null) {
            BigDecimal priceDifference = calculatedPrice.subtract(detail.expectedPrice()).abs();
            priceMatches = priceDifference.compareTo(new BigDecimal("1000")) <= 0;

            if (!priceMatches) {
                errors.add(String.format("Price mismatch for service %d. Expected: %s, Calculated: %s",
                        detail.serviceId(),
                        BookingDTOFormatter.formatPrice(detail.expectedPrice()),
                        BookingDTOFormatter.formatPrice(calculatedPrice)));
            }
        }
        // If expectedPrice is null, we skip price validation (will use calculated price)

        return ServiceValidationInfo.builder()
                .serviceId(detail.serviceId())
                .serviceName(service.getName())
                .exists(true)
                .active(service.getIsActive())
                .basePrice(service.getBasePrice())
                .validChoiceIds(validChoiceIds)
                .invalidChoiceIds(invalidChoiceIds)
                .calculatedPrice(calculatedPrice)
                .expectedPrice(detail.expectedPrice())
                .priceMatches(priceMatches)
                .recommendedStaff(Math.max(1, pricingResult.suggestedStaff()))
                .build();
    }

    private ServicePricingResult calculateServicePricing(BookingDetailRequest detail,
                                                         Service service,
                                                         List<Integer> validChoiceIds) {
        BigDecimal basePrice = service.getBasePrice() != null ? service.getBasePrice() : BigDecimal.ZERO;
        BigDecimal quantityMultiplier = BigDecimal.valueOf(detail.quantity() != null ? detail.quantity() : 1);
        BigDecimal fallbackTotal = detail.expectedPrice() != null
                ? detail.expectedPrice()
                : basePrice.multiply(quantityMultiplier);
        int fallbackStaff = service.getRecommendedStaff() != null ? service.getRecommendedStaff() : 1;
        BigDecimal fallbackUnitPrice = calculateUnitPrice(fallbackTotal, detail.quantity());

        try {
            // Create CalculatePriceRequest using existing ServiceService method
            List<Integer> selectionForPricing = (validChoiceIds != null && !validChoiceIds.isEmpty())
                    ? validChoiceIds
                    : (detail.selectedChoiceIds() != null ? detail.selectedChoiceIds() : List.of());

            CalculatePriceRequest priceRequest = new CalculatePriceRequest(
                    detail.serviceId(),
                    selectionForPricing,
                    detail.quantity()
            );

            // Call existing calculatePrice method
            CalculatePriceResponse priceResponse = serviceService.calculatePrice(priceRequest);

            if (Boolean.TRUE.equals(priceResponse.success()) && priceResponse.data() != null) {
                BigDecimal totalPrice = priceResponse.data().finalPrice();
                int suggestedStaff = priceResponse.data().suggestedStaff() != null
                        ? priceResponse.data().suggestedStaff()
                        : fallbackStaff;
                suggestedStaff = Math.max(1, suggestedStaff);
                BigDecimal calculatedUnitPrice = calculateUnitPrice(totalPrice, detail.quantity());
                return new ServicePricingResult(totalPrice, calculatedUnitPrice, suggestedStaff);
            }

            log.warn("Price calculation failed for service {}: {}", detail.serviceId(), priceResponse.message());
            return calculatePricingUsingRules(service, selectionForPricing, detail.quantity())
                    .orElseGet(() -> new ServicePricingResult(fallbackTotal, fallbackUnitPrice, Math.max(1, fallbackStaff)));

        } catch (Exception e) {
            log.error("Error calculating price for service {}: {}", detail.serviceId(), e.getMessage());
            return calculatePricingUsingRules(service,
                    validChoiceIds != null ? validChoiceIds : (detail.selectedChoiceIds() != null ? detail.selectedChoiceIds() : List.of()),
                    detail.quantity())
                    .orElseGet(() -> new ServicePricingResult(fallbackTotal, fallbackUnitPrice, Math.max(1, fallbackStaff)));
        }
    }

    private Optional<ServicePricingResult> calculatePricingUsingRules(Service service,
                                                                      List<Integer> selectedChoiceIds,
                                                                      Integer quantity) {
        try {
            List<Integer> safeChoiceIds = selectedChoiceIds != null ? selectedChoiceIds : List.of();
            List<PricingRule> applicableRules = findApplicablePricingRules(service.getServiceId(), safeChoiceIds);

            BigDecimal basePrice = service.getBasePrice() != null ? service.getBasePrice() : BigDecimal.ZERO;
            BigDecimal totalPriceAdjustment = applicableRules.stream()
                    .map(PricingRule::getPriceAdjustment)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal finalPrice = basePrice.add(totalPriceAdjustment);
            if (quantity != null && quantity > 1) {
                finalPrice = finalPrice.multiply(BigDecimal.valueOf(quantity));
            }

            int baseRecommendedStaff = service.getRecommendedStaff() != null ? service.getRecommendedStaff() : 1;
            int staffAdjustment = applicableRules.stream()
                    .map(PricingRule::getStaffAdjustment)
                    .filter(Objects::nonNull)
                    .reduce(0, Integer::sum);
            int suggestedStaff = Math.max(1, baseRecommendedStaff + staffAdjustment);

            BigDecimal unitPrice = calculateUnitPrice(finalPrice, quantity);

            return Optional.of(new ServicePricingResult(finalPrice, unitPrice, suggestedStaff));
        } catch (Exception ex) {
            log.error("Failed to calculate pricing using rules for service {}: {}", service.getServiceId(), ex.getMessage());
            return Optional.empty();
        }
    }

    private List<PricingRule> findApplicablePricingRules(Integer serviceId, List<Integer> selectedChoiceIds) {
        List<PricingRule> allRules = pricingRuleRepository.findByServiceIdOrderByPriorityDesc(serviceId);
        List<PricingRule> applicableRules = new ArrayList<>();

        for (PricingRule rule : allRules) {
            if (isRuleApplicable(rule, selectedChoiceIds)) {
                applicableRules.add(rule);
            }
        }

        return applicableRules;
    }

    private boolean isRuleApplicable(PricingRule rule, List<Integer> selectedChoiceIds) {
        List<Integer> requiredChoiceIds = ruleConditionRepository.findChoiceIdsByRuleId(rule.getId());

        if (requiredChoiceIds.isEmpty()) {
            return true; // Rule applies if no specific conditions
        }

        // Check if the rule applies based on condition logic
        if (rule.getConditionLogic() == ConditionLogic.ALL) {
            // ALL: All required choices must be selected
            return new HashSet<>(selectedChoiceIds).containsAll(requiredChoiceIds);
        } else if (rule.getConditionLogic() == ConditionLogic.ANY) {
            // ANY: At least one required choice must be selected
            return requiredChoiceIds.stream().anyMatch(selectedChoiceIds::contains);
        }

        return false;
    }

    private BigDecimal calculateUnitPrice(BigDecimal totalPrice, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return totalPrice;
        }

        return totalPrice.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
    }

    private int calculateRequiredEmployees(List<BookingDetailRequest> details,
                                           List<ServiceValidationInfo> serviceValidations) {
        Map<Integer, Integer> requiredStaffByService = serviceValidations.stream()
                .filter(ServiceValidationInfo::isValid)
                .collect(Collectors.toMap(
                        ServiceValidationInfo::getServiceId,
                        info -> info.getRecommendedStaff() != null ? info.getRecommendedStaff() : 1,
                        (existing, replacement) -> existing
                ));

        int totalRequiredEmployees = 0;
        for (BookingDetailRequest detail : details) {
            int recommendedStaff = requiredStaffByService.getOrDefault(detail.serviceId(), 1);
            totalRequiredEmployees += recommendedStaff * detail.quantity();
        }
        return totalRequiredEmployees;
    }

    private BigDecimal applyPromotion(String promoCode, BigDecimal amount, Customer customer, List<String> errors) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return amount;
        }

        Optional<Promotion> promotionOpt = promotionRepository.findAvailablePromotion(promoCode, LocalDateTime.now());
        if (promotionOpt.isEmpty()) {
            errors.add("Promotion code is invalid or expired: " + promoCode);
            return amount;
        }

        Promotion promotion = promotionOpt.get();

        // Check customer usage limit (if applicable)
        long customerUsage = promotionRepository.countPromoCodeUsageByCustomer(promoCode, customer.getCustomerId());
        if (customerUsage > 0) { // Assuming one-time use per customer
            errors.add("Promotion code has already been used by this customer");
            return amount;
        }

        // Calculate discount
        BigDecimal discount = BigDecimal.ZERO;
        if (promotion.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = promotion.getDiscountValue();
        } else if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = amount.multiply(promotion.getDiscountValue()).divide(new BigDecimal("100"));

            // Apply max discount limit
            if (promotion.getMaxDiscountAmount() != null &&
                    discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                discount = promotion.getMaxDiscountAmount();
            }
        }

        BigDecimal finalAmount = amount.subtract(discount);
        return finalAmount.max(BigDecimal.ZERO); // Ensure amount is not negative
    }

    private void validateEmployeeAssignments(List<AssignmentRequest> assignments,
                                             LocalDateTime bookingTime,
                                             List<ConflictInfo> conflicts) {
        for (AssignmentRequest assignment : assignments) {
            Employee employee = employeeRepository.findById(assignment.employeeId()).orElse(null);
            if (employee == null) {
                conflicts.add(new ConflictInfo(
                        "EMPLOYEE_NOT_FOUND",
                        assignment.employeeId(),
                        bookingTime,
                        bookingTime,
                        "Employee not found"
                ));
                continue;
            }

            // Check employee availability
            checkEmployeeAvailability(employee, assignment.serviceId(), bookingTime, conflicts);
        }
    }

    private void checkEmployeeAvailability(Employee employee, Integer serviceId,
                                           LocalDateTime bookingTime,
                                           List<ConflictInfo> conflicts) {
        // Get service duration for time range calculation
        Optional<Service> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isEmpty()) return;

        var service = serviceOpt.get();
        LocalDateTime endTime = bookingTime.plusHours(service.getEstimatedDurationHours().longValue());

        // Check for conflicting assignments
        List<Assignment> conflictingAssignments = assignmentRepository.findConflictingAssignments(
                employee.getEmployeeId(), bookingTime, endTime, null);

        if (!conflictingAssignments.isEmpty()) {
            Assignment conflict = conflictingAssignments.get(0);

            // Try to get employee name safely
            String employeeName = "Unknown Employee";
            try {
                if (employee.getAccount() != null && employee.getFullName() != null) {
                    employeeName = employee.getFullName();
                } else if (employee.getFullName() != null) {
                    employeeName = employee.getFullName();
                }
            } catch (Exception e) {
                log.warn("Could not get employee name for {}: {}", employee.getEmployeeId(), e.getMessage());
            }

            conflicts.add(new ConflictInfo(
                    "ASSIGNMENT_CONFLICT",
                    employee.getEmployeeId(),
                    conflict.getBookingDetail().getBooking().getBookingTime(),
                    conflict.getBookingDetail().getBooking().getBookingTime().plusHours(
                            conflict.getBookingDetail().getService().getEstimatedDurationHours().longValue()),
                    "Employee " + employeeName + " has another assignment during this time"
            ));
        }
    }

    private void validateEmployeeAvailabilityFinal(BookingCreateRequest request) {
        // Final check for employee availability right before saving
        List<ConflictInfo> conflicts = new ArrayList<>();
        validateEmployeeAssignments(request.assignments(), request.bookingTime(), conflicts);

        if (!conflicts.isEmpty()) {
            throw EmployeeConflictException.withConflicts(conflicts);
        }
    }

    private Booking createBookingEntity(BookingCreateRequest request, BookingValidationResult validation) {
        Booking booking = new Booking();

        // Set basic fields
        booking.setBookingTime(request.bookingTime());
        booking.setNote(request.note());
        booking.setTotalAmount(validation.getCalculatedTotalAmount());
        booking.setStatus(BookingStatus.PENDING);

        // Set customer and address
        Address bookingAddress = validation.getAddress();
        if (validation.isUsingNewAddress()) {
            Address newAddress = bookingAddress;
            if (newAddress.getCustomer() == null && validation.getCustomer() != null) {
                newAddress.setCustomer(validation.getCustomer());
            }
            bookingAddress = addressRepository.save(newAddress);
        } else if (bookingAddress == null || bookingAddress.getAddressId() == null) {
            bookingAddress = addressRepository.findById(request.addressId())
                    .orElseThrow(() -> AddressNotFoundException.withId(request.addressId()));
        }

        booking.setAddress(bookingAddress);
        if (validation.getCustomer() != null) {
            booking.setCustomer(validation.getCustomer());
        } else if (bookingAddress.getCustomer() != null) {
            booking.setCustomer(bookingAddress.getCustomer());
        }


        // Set promotion if applicable
        if (request.promoCode() != null && !request.promoCode().trim().isEmpty()) {
            promotionRepository.findByPromoCode(request.promoCode())
                    .ifPresent(booking::setPromotion);
        }

        return booking;
    }

    private List<BookingDetail> createBookingDetails(Booking booking,
                                                     BookingCreateRequest request,
                                                     BookingValidationResult validation) {
        List<BookingDetail> details = new ArrayList<>();

        for (int i = 0; i < request.bookingDetails().size(); i++) {
            BookingDetailRequest detailRequest = request.bookingDetails().get(i);
            ServiceValidationInfo serviceValidation = validation.getServiceValidations().get(i);

            BookingDetail detail = new BookingDetail();
            detail.setBooking(booking);

            // Set service
            Service service = serviceRepository.findById(detailRequest.serviceId())
                    .orElseThrow(() -> ServiceNotFoundException.withId(detailRequest.serviceId()));
            detail.setService(service);

            detail.setQuantity(detailRequest.quantity());
            BigDecimal calculatedPrice = serviceValidation.getCalculatedPrice();
            BigDecimal pricePerUnit = calculateUnitPrice(calculatedPrice, detailRequest.quantity());
            detail.setPricePerUnit(pricePerUnit);
            detail.setSubTotal(calculatedPrice);

            // Set selected choice IDs as comma-separated string
            if (detailRequest.selectedChoiceIds() != null && !detailRequest.selectedChoiceIds().isEmpty()) {
                detail.setSelectedChoiceIds(String.join(",",
                        detailRequest.selectedChoiceIds().stream()
                                .map(String::valueOf)
                                .toList()));
            }

            details.add(detail);
        }

        return details;
    }

    private List<BookingAdditionalFee> createBookingAdditionalFees(Booking booking, BookingValidationResult validation) {
        List<BookingAdditionalFee> fees = new ArrayList<>();
        if (validation.getFeeBreakdowns() == null || validation.getFeeBreakdowns().isEmpty()) {
            return fees;
        }
        for (FeeBreakdownInfo info : validation.getFeeBreakdowns()) {
            BookingAdditionalFee applied = new BookingAdditionalFee();
            applied.setBooking(booking);
            applied.setFeeName(info.getName());
            applied.setFeeType(info.getType());
            applied.setFeeValue(info.getValue());
            applied.setFeeAmount(info.getAmount());
            applied.setSystemSurcharge(info.isSystemSurcharge());
            booking.addAdditionalFee(applied);
            fees.add(applied);
        }
        return fees;
    }

    private List<Assignment> createAssignments(List<BookingDetail> bookingDetails,
                                               BookingCreateRequest request) {
        List<Assignment> assignments = new ArrayList<>();

        // Group assignments by service ID
        Map<Integer, List<AssignmentRequest>> assignmentsByService = new HashMap<>();
        for (AssignmentRequest assignment : request.assignments()) {
            assignmentsByService.computeIfAbsent(assignment.serviceId(), k -> new ArrayList<>())
                    .add(assignment);
        }

        for (BookingDetail detail : bookingDetails) {
            List<AssignmentRequest> serviceAssignments = assignmentsByService.get(detail.getService().getServiceId());

            if (serviceAssignments != null) {
                for (AssignmentRequest assignmentRequest : serviceAssignments) {
                    Employee employee = employeeRepository.findById(assignmentRequest.employeeId())
                            .orElseThrow(() -> EmployeeNotFoundException.withId(assignmentRequest.employeeId()));

                    Assignment assignment = new Assignment();
                    assignment.setBookingDetail(detail);
                    assignment.setEmployee(employee);
                    assignment.setStatus(AssignmentStatus.ASSIGNED);

                    assignments.add(assignment);
                }
            }
        }

        return assignments;
    }

    private Payment createPaymentRecord(Booking booking, int paymentMethodId) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow());

        // Generate simple transaction code
        payment.setTransactionCode("TXN_" + System.currentTimeMillis());

        return payment;
    }

    private BookingCreationSummary createBookingCreationSummary(Booking booking,
                                                                List<BookingDetail> details,
                                                                List<Assignment> assignments,
                                                                Payment payment,
                                                                boolean hasAutoAssignedEmployees) {
        // Group assignments by BookingDetail ID
        Map<String, List<Assignment>> assignmentsByDetailId = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getBookingDetail().getId()));
        
        // Map to DTOs - manually set assignments for each detail
        CustomerAddressInfo addressInfo = bookingMapper.toCustomerAddressInfo(booking.getAddress());
        List<BookingDetailInfo> detailInfos = details.stream()
                .map(detail -> {
                    // Get assignments for this detail
                    List<Assignment> detailAssignments = assignmentsByDetailId.getOrDefault(detail.getId(), List.of());
                    // Set assignments to detail so mapper can use them
                    detail.setAssignments(detailAssignments);
                    return bookingMapper.toBookingDetailInfo(detail);
                })
                .toList();
        PaymentInfo paymentInfo = bookingMapper.toPaymentInfo(payment);
        PromotionInfo promotionInfo = booking.getPromotion() != null ?
                bookingMapper.toPromotionInfo(booking.getPromotion()) : null;

        BigDecimal feeTotal = booking.getAdditionalFees() != null
                ? booking.getAdditionalFees().stream()
                    .map(BookingAdditionalFee::getFeeAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
        BigDecimal baseAmount = booking.getTotalAmount() != null
                ? booking.getTotalAmount().subtract(feeTotal)
                : null;
        List<FeeBreakdownResponse> fees = booking.getAdditionalFees() != null
                ? booking.getAdditionalFees().stream()
                    .map(f -> FeeBreakdownResponse.builder()
                            .name(f.getFeeName())
                            .type(f.getFeeType())
                            .value(f.getFeeValue())
                            .amount(f.getFeeAmount())
                            .systemSurcharge(f.isSystemSurcharge())
                            .build())
                    .toList()
                : List.of();

        // Create summary
        BookingCreationSummary summary = BookingCreationSummary.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus().toString())
                .totalAmount(booking.getTotalAmount())
                .formattedTotalAmount(BookingDTOFormatter.formatPrice(booking.getTotalAmount()))
                .baseAmount(baseAmount)
                .totalFees(feeTotal)
                .fees(fees)
                .bookingTime(booking.getBookingTime())
                .title(booking.getTitle())
                .imageUrls(booking.getImageUrls())
                .isVerified(booking.getIsVerified())
                .adminComment(booking.getAdminComment())
                .customerInfo(addressInfo)
                .serviceDetails(detailInfos)
                .paymentInfo(paymentInfo)
                .promotionApplied(promotionInfo)
                .assignedEmployees(assignments.stream()
                        .map(a -> bookingMapper.toEmployeeInfo(a.getEmployee()))
                        .toList())
                .createdAt(booking.getCreatedAt())
                .hasAutoAssignedEmployees(hasAutoAssignedEmployees)
                .build();

        try {
            summary.calculateSummaryFields();
        } catch (Exception e) {
            log.warn("Could not calculate summary fields: {}", e.getMessage());
        }

        return summary;
    }

    private void notifyAssignedEmployees(List<Assignment> assignments, Booking booking) {
        if (assignments == null || assignments.isEmpty() || booking == null) {
            return;
        }

        String bookingCode = booking.getBookingCode();
        String fallbackBookingIdentifier = booking.getBookingId();

        assignments.stream()
                .filter(Objects::nonNull)
                .forEach(assignment -> {
                    try {
                        Employee employee = assignment.getEmployee();
                        if (employee == null || employee.getAccount() == null) {
                            log.warn("Skip notification for assignment {} because employee/account is missing",
                                    assignment.getAssignmentId());
                            return;
                        }

                        notificationService.sendAssignmentCreatedNotification(
                                employee.getAccount().getAccountId(),
                                assignment.getAssignmentId(),
                                bookingCode != null ? bookingCode : fallbackBookingIdentifier
                        );
                    } catch (Exception ex) {
                        log.error("Failed to send assignment notification for assignment {}: {}",
                                assignment.getAssignmentId(), ex.getMessage(), ex);
                    }
                });
    }

    private void notifyEmployeesBookingCancelled(List<Assignment> assignments, Booking booking, String reason) {
        if (assignments == null || assignments.isEmpty() || booking == null) {
            return;
        }

        String bookingIdentifier = booking.getBookingCode() != null
                ? booking.getBookingCode()
                : booking.getBookingId();
        String normalizedReason = normalizeReason(reason, "Khách hàng không cung cấp lý do");

        Map<String, Assignment> assignmentByAccount = new HashMap<>();
        assignments.stream()
                .filter(Objects::nonNull)
                .forEach(assignment -> {
                    Employee employee = assignment.getEmployee();
                    if (employee == null || employee.getAccount() == null) {
                        log.warn("Skip cancellation notification for assignment {} because employee/account is missing",
                                assignment.getAssignmentId());
                        return;
                    }

                    String accountId = employee.getAccount().getAccountId();
                    assignmentByAccount.putIfAbsent(accountId, assignment);
                });

        assignmentByAccount.forEach((accountId, assignment) -> {
            try {
                notificationService.sendAssignmentCancelledNotificationForEmployee(
                        accountId,
                        assignment.getAssignmentId(),
                        bookingIdentifier,
                        normalizedReason
                );
            } catch (Exception ex) {
                log.error("Failed to send cancellation notification for assignment {}: {}",
                        assignment.getAssignmentId(), ex.getMessage(), ex);
            }
        });
    }

    private void notifyCustomerBookingCreated(Booking booking) {
        resolveCustomerAccountId(booking).ifPresent(accountId ->
                notificationService.sendBookingCreatedNotification(
                        accountId,
                        booking.getBookingId(),
                        booking.getBookingCode()
                ));
    }

    private void notifyCustomerBookingCancelled(Booking booking, String reason) {
        resolveCustomerAccountId(booking).ifPresent(accountId ->
                notificationService.sendBookingCancelledNotification(
                        accountId,
                        booking.getBookingId(),
                        booking.getBookingCode(),
                        normalizeReason(reason, "Khách hàng không cung cấp lý do")
                ));
    }

    private void notifyBookingVerificationResult(Booking booking, boolean approved) {
        resolveCustomerAccountId(booking).ifPresent(accountId ->
                notificationService.sendBookingVerifiedNotification(
                        accountId,
                        booking.getBookingId(),
                        booking.getBookingCode(),
                        approved
                ));
    }

    private Optional<String> resolveCustomerAccountId(Booking booking) {
        if (booking == null || booking.getCustomer() == null || booking.getCustomer().getAccount() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(booking.getCustomer().getAccount().getAccountId());
    }

    private void publishBookingStatusEvent(Booking booking, String trigger, String note) {
        if (booking == null) {
            return;
        }
        bookingRealtimeEventPublisher.publishBookingStatus(
                BookingStatusWebSocketEvent.builder()
                        .bookingId(booking.getBookingId())
                        .bookingCode(booking.getBookingCode())
                        .status(booking.getStatus())
                        .trigger(trigger)
                        .note(note)
                        .at(LocalDateTime.now())
                        .build()
        );
    }

    private String normalizeReason(String reason, String fallback) {
        return (reason != null && !reason.trim().isEmpty()) ? reason.trim() : fallback;
    }

    private record CustomerAddressContext(Customer customer, Address address, boolean isNewAddress) {
    }

    private record ValidationOutcome(BookingValidationResult result, CustomerAddressContext addressContext) {
    }

    private record ServicePricingResult(BigDecimal totalPrice, BigDecimal unitPrice, int suggestedStaff) {
    }

    @Override
    public Page<BookingHistoryResponse> getBookingsByCustomerId(String customerId, Pageable pageable) {
        log.info("Fetching bookings for customer {} with pagination", customerId);

        // First verify customer exists
        if (!customerRepository.existsById(customerId)) {
            log.warn("Customer {} not found", customerId);
            return Page.empty(pageable);
        }

        // Fetch paginated bookings for the customer
        Page<Booking> bookingPage = bookingRepository.findByCustomerIdWithPagination(customerId, pageable);

        if (bookingPage.isEmpty()) {
            log.info("No bookings found for customer {}", customerId);
            return Page.empty(pageable);
        }

        Page<BookingHistoryResponse> bookingHistoryResponsePage = bookingPage.map(booking -> {
            CustomerAddressInfo addressInfo = bookingMapper.toCustomerAddressInfo(booking.getAddress());
            PaymentInfo paymentInfo = booking.getPayments().isEmpty() ? null : bookingMapper.toPaymentInfo(booking.getPayments().get(0));
            PromotionInfo promotionInfo = booking.getPromotion() != null ? bookingMapper.toPromotionInfo(booking.getPromotion()) : null;

            // Get all assignments for this booking
            List<EmployeeInfo> assignedEmployees = booking.getBookingDetails().stream()
                    .flatMap(detail -> detail.getAssignments().stream())
                    .map(assignment -> bookingMapper.toEmployeeInfo(assignment.getEmployee()))
                    .distinct()
                    .toList();

            // Get all services for this booking
            List<ServiceInfo> services = booking.getBookingDetails().stream()
                    .map(detail -> bookingMapper.toServiceInfo(detail.getService()))
                    .distinct()
                    .toList();

            BigDecimal feeTotal = booking.getAdditionalFees() != null
                    ? booking.getAdditionalFees().stream()
                        .map(BookingAdditionalFee::getFeeAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;
            BigDecimal baseAmount = booking.getTotalAmount() != null
                    ? booking.getTotalAmount().subtract(feeTotal)
                    : null;
            List<FeeBreakdownResponse> fees = booking.getAdditionalFees() != null
                    ? booking.getAdditionalFees().stream()
                        .map(f -> FeeBreakdownResponse.builder()
                                .name(f.getFeeName())
                                .type(f.getFeeType())
                                .value(f.getFeeValue())
                                .amount(f.getFeeAmount())
                                .systemSurcharge(f.isSystemSurcharge())
                                .build())
                        .toList()
                    : List.of();

            return new BookingHistoryResponse(
                    booking.getBookingId(),
                    booking.getBookingCode(),
                    booking.getCustomer().getCustomerId(),
                    booking.getCustomer().getFullName(),
                    addressInfo,
                    booking.getBookingTime().toString(),
                    booking.getNote(),
                    booking.getTotalAmount(),
                    BookingDTOFormatter.formatPrice(booking.getTotalAmount()),
                    booking.getStatus().toString(),
                    promotionInfo,
                    paymentInfo,
                    booking.getTitle(),
                    booking.getImageUrls(),
                    booking.getIsVerified(),
                    assignedEmployees,
                    services,
                    baseAmount,
                    feeTotal,
                    fees
            );
        });

        log.info("Found {} bookings for customer {} (page {} of {})",
                bookingPage.getNumberOfElements(),
                customerId,
                bookingPage.getNumber() + 1,
                bookingPage.getTotalPages());

        return bookingHistoryResponsePage;
    }

    @Override
    public Page<BookingHistoryResponse> getBookingsByCustomerId(String customerId, LocalDateTime fromDate, Pageable pageable) {
        log.info("Fetching bookings for customer {} from date {} with pagination", customerId, fromDate);

        // First verify customer exists
        if (!customerRepository.existsById(customerId)) {
            log.warn("Customer {} not found", customerId);
            return Page.empty(pageable);
        }

        // Fetch paginated bookings for the customer with date filter
        Page<Booking> bookingPage;
        if (fromDate != null) {
            bookingPage = bookingRepository.findByCustomerIdWithPaginationAndDate(customerId, fromDate, pageable);
        } else {
            bookingPage = bookingRepository.findByCustomerIdWithPagination(customerId, pageable);
        }

        if (bookingPage.isEmpty()) {
            log.info("No bookings found for customer {} from date {}", customerId, fromDate);
            return Page.empty(pageable);
        }

        Page<BookingHistoryResponse> bookingHistoryResponsePage = bookingPage.map(booking -> {
            CustomerAddressInfo addressInfo = bookingMapper.toCustomerAddressInfo(booking.getAddress());
            PaymentInfo paymentInfo = booking.getPayments().isEmpty() ? null : bookingMapper.toPaymentInfo(booking.getPayments().get(0));
            PromotionInfo promotionInfo = booking.getPromotion() != null ? bookingMapper.toPromotionInfo(booking.getPromotion()) : null;

            // Get all assignments for this booking
            List<EmployeeInfo> assignedEmployees = booking.getBookingDetails().stream()
                    .flatMap(detail -> detail.getAssignments().stream())
                    .map(assignment -> bookingMapper.toEmployeeInfo(assignment.getEmployee()))
                    .distinct()
                    .toList();

            // Get all services for this booking
            List<ServiceInfo> services = booking.getBookingDetails().stream()
                    .map(detail -> bookingMapper.toServiceInfo(detail.getService()))
                    .distinct()
                    .toList();

            BigDecimal feeTotal = booking.getAdditionalFees() != null
                    ? booking.getAdditionalFees().stream()
                        .map(BookingAdditionalFee::getFeeAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;
            BigDecimal baseAmount = booking.getTotalAmount() != null
                    ? booking.getTotalAmount().subtract(feeTotal)
                    : null;
            List<FeeBreakdownResponse> fees = booking.getAdditionalFees() != null
                    ? booking.getAdditionalFees().stream()
                        .map(f -> FeeBreakdownResponse.builder()
                                .name(f.getFeeName())
                                .type(f.getFeeType())
                                .value(f.getFeeValue())
                                .amount(f.getFeeAmount())
                                .systemSurcharge(f.isSystemSurcharge())
                                .build())
                        .toList()
                    : List.of();

            return new BookingHistoryResponse(
                    booking.getBookingId(),
                    booking.getBookingCode(),
                    booking.getCustomer().getCustomerId(),
                    booking.getCustomer().getFullName(),
                    addressInfo,
                    booking.getBookingTime().toString(),
                    booking.getNote(),
                    booking.getTotalAmount(),
                    BookingDTOFormatter.formatPrice(booking.getTotalAmount()),
                    booking.getStatus().toString(),
                    promotionInfo,
                    paymentInfo,
                    booking.getTitle(),
                    booking.getImageUrls(),
                    booking.getIsVerified(),
                    assignedEmployees,
                    services,
                    baseAmount,
                    feeTotal,
                    fees
            );
        });

        log.info("Found {} bookings for customer {} from date {} (page {} of {})",
                bookingPage.getNumberOfElements(),
                customerId,
                fromDate,
                bookingPage.getNumber() + 1,
                bookingPage.getTotalPages());

        return bookingHistoryResponsePage;
    }

    @Override
    @Transactional
    public BookingResponse convertBookingToPost(String bookingId, ConvertBookingToPostRequest request) {
        log.info("Updating booking {} with title: {}, imageUrls: {}", bookingId, request.title(), request.imageUrls());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("KhĂ´ng tĂ¬m tháº¥y booking vá»›i ID: " + bookingId));

        // Verify booking is unverified (meaning it has no assignments)
        if (booking.getIsVerified()) {
            throw new IllegalStateException("KhĂ´ng thá»ƒ cáº­p nháº­t booking Ä‘Ă£ Ä‘Æ°á»£c xĂ¡c minh (cĂ³ nhĂ¢n viĂªn)");
        }

        // Update title and image URLs
        booking.setTitle(request.title());
        booking.setImageUrls(request.imageUrls());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Successfully updated booking {} title and images", bookingId);

        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getUnverifiedBookings(Pageable pageable) {
        log.info("Fetching unverified bookings for admin review");

        Page<Booking> unverifiedBookings = bookingRepository.findUnverifiedBookingsOrderByCreatedAtDesc(pageable);

        Page<BookingResponse> response = unverifiedBookings.map(bookingMapper::toBookingResponse);

        log.info("Found {} unverified bookings (page {} of {})",
                unverifiedBookings.getNumberOfElements(),
                unverifiedBookings.getNumber() + 1,
                unverifiedBookings.getTotalPages());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getUnverifiedBookings(LocalDateTime fromDate, Pageable pageable) {
        log.info("Fetching unverified bookings for admin review with fromDate: {}", fromDate);

        Page<Booking> unverifiedBookings;
        if (fromDate != null) {
            unverifiedBookings = bookingRepository.findUnverifiedBookingsOrderByCreatedAtDescWithDate(fromDate, pageable);
        } else {
            unverifiedBookings = bookingRepository.findUnverifiedBookingsOrderByCreatedAtDesc(pageable);
        }

        Page<BookingResponse> response = unverifiedBookings.map(bookingMapper::toBookingResponse);

        log.info("Found {} unverified bookings (page {} of {})",
                unverifiedBookings.getNumberOfElements(),
                unverifiedBookings.getNumber() + 1,
                unverifiedBookings.getTotalPages());

        return response;
    }

    @Override
    @Transactional
    public BookingResponse verifyBooking(String bookingId, BookingVerificationRequest request) {
        log.info("Admin verifying booking {}: approve={}", bookingId, request.approve());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking với ID: " + bookingId));

        if (booking.getIsVerified()) {
            throw new IllegalStateException("Booking này đã được xác minh trước đó");
        }

        if (request.approve()) {
            // Approve the booking post
            booking.setIsVerified(true);

            // Save admin comment if provided
            if (request.adminComment() != null && !request.adminComment().trim().isEmpty()) {
                booking.setAdminComment(request.adminComment());
                log.info("Booking {} approved with admin comment", bookingId);
            } else {
                log.info("Booking {} has been approved by admin", bookingId);
            }

            // Notification to customer is handled via notifyBookingVerificationResult(...)
        } else {
            // Reject the booking post
            log.info("Booking {} has been rejected by admin. Reason: {}",
                    bookingId, request.rejectionReason());

            // Save rejection reason as admin comment
            if (request.rejectionReason() != null && !request.rejectionReason().trim().isEmpty()) {
                booking.setAdminComment(request.rejectionReason());
            }

            booking.setIsVerified(true);

            // Notification to customer (with rejection reason) handled via notifyBookingVerificationResult(...)
            // For now, we'll just cancel the booking
            booking.setStatus(BookingStatus.CANCELLED);
        }

        Booking savedBooking = bookingRepository.save(booking);
        notifyBookingVerificationResult(savedBooking, request.approve());
        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(String bookingId, String customerId, String reason) {
        log.info("Customer {} cancelling booking {}", customerId, bookingId);

        // 1. Find booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking {} not found", bookingId);
                    return BookingNotFoundException.withId(bookingId);
                });

        // 2. Verify booking belongs to customer
        if (!booking.getCustomer().getCustomerId().equals(customerId)) {
            log.error("Customer {} tried to cancel booking {} which belongs to customer {}",
                    customerId, bookingId, booking.getCustomer().getCustomerId());
            throw BookingValidationException.singleError("Bạn không có quyền hủy booking này");
        }

        // 3. Check if booking can be cancelled
        BookingStatus currentStatus = booking.getStatus();

        if (currentStatus == BookingStatus.CANCELLED) {
            log.warn("Booking {} is already cancelled", bookingId);
            throw BookingValidationException.singleError("Booking đã bị hủy trước đó");
        }

        if (currentStatus == BookingStatus.COMPLETED) {
            log.warn("Cannot cancel completed booking {}", bookingId);
            throw BookingValidationException.singleError("Không thể hủy booking đã hoàn thành");
        }

        if (currentStatus == BookingStatus.IN_PROGRESS) {
            log.warn("Cannot cancel in-progress booking {}", bookingId);
            throw BookingValidationException.singleError("Không thể hủy booking đang thực hiện");
        }

        // 4. Cancel the booking
        booking.setStatus(BookingStatus.CANCELLED);

        // 5. Save cancellation reason
        if (reason != null && !reason.trim().isEmpty()) {
            String cancelNote = "Khách hàng hủy: " + reason.trim();
            if (booking.getAdminComment() != null && !booking.getAdminComment().isEmpty()) {
                booking.setAdminComment(booking.getAdminComment() + " | " + cancelNote);
            } else {
                booking.setAdminComment(cancelNote);
            }
        } else {
            String cancelNote = "Khách hàng hủy booking";
            if (booking.getAdminComment() != null && !booking.getAdminComment().isEmpty()) {
                booking.setAdminComment(booking.getAdminComment() + " | " + cancelNote);
            } else {
                booking.setAdminComment(cancelNote);
            }
        }

        // 6. Cancel all assignments related to this booking
        List<Assignment> cancelledAssignments = new ArrayList<>();
        List<BookingDetail> bookingDetails = booking.getBookingDetails();
        if (bookingDetails != null && !bookingDetails.isEmpty()) {
            for (BookingDetail detail : bookingDetails) {
                List<Assignment> assignments = assignmentRepository.findByBookingDetailId(detail.getId());
                if (assignments != null && !assignments.isEmpty()) {
                    for (Assignment assignment : assignments) {
                        if (assignment.getStatus() != AssignmentStatus.CANCELLED &&
                                assignment.getStatus() != AssignmentStatus.COMPLETED) {
                            assignment.setStatus(AssignmentStatus.CANCELLED);
                            Assignment savedAssignment = assignmentRepository.save(assignment);
                            cancelledAssignments.add(savedAssignment);

                            String employeeId = savedAssignment.getEmployee() != null
                                    ? savedAssignment.getEmployee().getEmployeeId()
                                    : "unknown";
                            log.info("Cancelled assignment {} for employee {}",
                                    savedAssignment.getAssignmentId(),
                                    employeeId);
                        }
                    }
                }
            }
        }

        // 7. Handle payment refund/cancellation
        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        if (payments != null && !payments.isEmpty()) {
            for (Payment payment : payments) {
                if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                    // Mark for refund
                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                    paymentRepository.save(payment);
                    log.info("Marked payment {} for refund", payment.getId());
                } else if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
                    // Cancel pending payment
                    payment.setPaymentStatus(PaymentStatus.CANCELLED);
                    paymentRepository.save(payment);
                    log.info("Cancelled pending payment {}", payment.getId());
                }
            }
        }

        // 8. Save booking
        Booking savedBooking = bookingRepository.save(booking);
        publishBookingStatusEvent(savedBooking, "CUSTOMER_CANCEL", reason);

        log.info("Booking {} cancelled successfully by customer {}", bookingId, customerId);
        notifyCustomerBookingCancelled(savedBooking, reason);
        notifyEmployeesBookingCancelled(cancelledAssignments, savedBooking, reason);

        // TODO: Process actual refund through payment gateway

        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getVerifiedAwaitingEmployeeBookings(String employeeId, boolean matchEmployeeZones, Pageable pageable) {
        return getVerifiedAwaitingEmployeeBookings(employeeId, matchEmployeeZones, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getVerifiedAwaitingEmployeeBookings(String employeeId,
                                                                     boolean matchEmployeeZones,
                                                                     LocalDateTime fromDate,
                                                                     Pageable pageable) {
        log.info("Fetching recommended verified bookings awaiting employee for employeeId={} from date {} (matchEmployeeZones={})",
                employeeId, fromDate, matchEmployeeZones);

        List<Booking> awaitingBookings = fromDate != null
                ? bookingRepository.findAllVerifiedAwaitingEmployeeBookingsFromDate(fromDate)
                : bookingRepository.findAllVerifiedAwaitingEmployeeBookings();

        if (employeeId == null || employeeId.isBlank()) {
            log.debug("No employee context provided; returning bookings ordered by booking time");
            return toRecommendationPage(awaitingBookings, pageable);
        }

        Employee employee = employeeRepository.findEmployeeWithDetails(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));

        List<Booking> rankedBookings = rankBookingsForEmployee(employee, awaitingBookings, matchEmployeeZones);

        log.info("Generated ranked booking list for employee {} with {} candidate bookings (matchEmployeeZones={})",
                employeeId, rankedBookings.size(), matchEmployeeZones);

        return toRecommendationPage(rankedBookings, pageable);
    }

    private List<Booking> rankBookingsForEmployee(Employee employee,
                                                  List<Booking> awaitingBookings,
                                                  boolean matchEmployeeZones) {
        if (awaitingBookings == null || awaitingBookings.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> normalizedZones = Optional.ofNullable(employee.getWorkingZones())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(zone -> buildZoneKey(zone.getWard(), zone.getCity()))
                .filter(key -> !key.isEmpty())
                .collect(Collectors.toSet());

        log.debug("Employee {} has {} working zones: {}",
                employee.getEmployeeId(),
                normalizedZones.size(),
                normalizedZones);

        List<BookingRecommendationScore> scoredBookings = awaitingBookings.stream()
                .map(booking -> computeRecommendationScore(booking, normalizedZones))
                .collect(Collectors.toList());

        boolean hasDefinedZones = !normalizedZones.isEmpty();
        boolean anyZoneMatches = scoredBookings.stream().anyMatch(BookingRecommendationScore::zoneMatched);
        boolean filterToMatches = matchEmployeeZones && hasDefinedZones;
        boolean prioritiseZoneMatches = false;

        if (matchEmployeeZones) {
            if (!hasDefinedZones) {
                log.debug("Employee {} has no working zones; unable to apply zone filter", employee.getEmployeeId());
            } else if (!anyZoneMatches) {
                log.debug("Employee {} requested zone-only bookings but no matches were found for zones {}", employee.getEmployeeId(), normalizedZones);
            }
        }

        log.debug("Ranking {} awaiting bookings for employee {} across {} working zones (filterToMatches={}, prioritiseZoneMatches={})",
                awaitingBookings.size(),
                employee.getEmployeeId(),
                normalizedZones.size(),
                filterToMatches,
                prioritiseZoneMatches);

        Comparator<BookingRecommendationScore> comparator = Comparator
                .comparingDouble(BookingRecommendationScore::score).reversed()
                .thenComparing(BookingRecommendationScore::timeScore, Comparator.reverseOrder())
                .thenComparing(BookingRecommendationScore::ratingScore, Comparator.reverseOrder())
                .thenComparing(score -> score.booking().getBookingTime(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(score -> score.booking().getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()));

        return scoredBookings.stream()
                .filter(score -> !filterToMatches || score.zoneMatched())
                .filter(score -> !prioritiseZoneMatches || score.zoneMatched())
                .sorted(comparator)
                .map(BookingRecommendationScore::booking)
                .collect(Collectors.toList());
    }

    private Page<BookingResponse> toRecommendationPage(List<Booking> orderedBookings, Pageable pageable) {
        List<Booking> safeBookings = Optional.ofNullable(orderedBookings).orElseGet(Collections::emptyList);
        if (pageable == null || pageable.isUnpaged()) {
            List<BookingResponse> content = safeBookings.stream()
                    .map(bookingMapper::toBookingResponse)
                    .collect(Collectors.toList());
            return new PageImpl<>(content);
        }

        int total = safeBookings.size();
        int start = Math.min((int) pageable.getOffset(), total);
        int end = Math.min(start + pageable.getPageSize(), total);
        List<BookingResponse> content = safeBookings.subList(start, end).stream()
                .map(bookingMapper::toBookingResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    private BookingRecommendationScore computeRecommendationScore(Booking booking, Set<String> normalizedZones) {
        String bookingZoneKey = buildZoneKey(
                booking.getAddress() != null ? booking.getAddress().getWard() : null,
                booking.getAddress() != null ? booking.getAddress().getCity() : null);

        boolean hasDefinedZones = normalizedZones != null && !normalizedZones.isEmpty();
        boolean zoneMatched = hasDefinedZones ? normalizedZones.contains(bookingZoneKey) : true;

        double locationScore = zoneMatched ? 1.0 : hasDefinedZones ? 0.0 : 0.5;
        double timeScore = calculateTimeScore(booking.getBookingTime());
        double ratingScore = calculateRatingScore(booking.getCustomer());

        double combinedScore = (0.5 * locationScore) + (0.3 * timeScore) + (0.2 * ratingScore);
        if (hasDefinedZones && !zoneMatched) {
            combinedScore *= 0.35;
        }

        return new BookingRecommendationScore(booking, combinedScore, zoneMatched, timeScore, ratingScore);
    }

    private double calculateTimeScore(LocalDateTime bookingTime) {
        if (bookingTime == null) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesUntil = Duration.between(now, bookingTime).toMinutes();
        if (minutesUntil <= 0) {
            return 0.2;
        }

        double hoursUntil = minutesUntil / 60.0;
        double score = Math.exp(-hoursUntil / 24.0);
        return Math.min(Math.max(score, 0.0), 1.0);
    }

    private double calculateRatingScore(Customer customer) {
        if (customer == null || customer.getRating() == null) {
            return 0.5;
        }

        return switch (customer.getRating()) {
            case LOWEST -> 0.2;
            case LOW -> 0.4;
            case MEDIUM -> 0.6;
            case HIGH -> 0.8;
            case HIGHEST -> 1.0;
        };
    }

    private String buildZoneKey(String ward, String city) {
        String normalizedWard = normalizeText(ward);
        String normalizedCity = normalizeText(city);
        if (normalizedWard.isEmpty() && normalizedCity.isEmpty()) {
            return "";
        }
        return normalizedWard + "|" + normalizedCity;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.trim().toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
        // String noDiacritics = normalized.replaceAll("\\p{M}", "");
        // noDiacritics = noDiacritics
        //         .replace("tphcm", "thanh pho ho chi minh")
        //         .replace("tp ho chi minh", "thanh pho ho chi minh")
        //         .replace("tp. ho chi minh", "thanh pho ho chi minh")
        //         .replace("tp ho. chi minh", "thanh pho ho chi minh")
        //         .replace("ho chi minh city", "thanh pho ho chi minh")
        //         .replace("tp hcm", "thanh pho ho chi minh")
        //         .replace("tp.", "thanh pho")
        //         .replace("tp ", "thanh pho ");
        // noDiacritics = noDiacritics
        //         .replace('đ', 'd')
        //         .replaceAll("[^a-z0-9\\s]", " ");
        // return noDiacritics.replaceAll("\\s+", " ").trim();
    }

    private record BookingRecommendationScore(
            Booking booking,
            double score,
            boolean zoneMatched,
            double timeScore,
            double ratingScore
    ) {
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookingsSortedByBookingTime(Pageable pageable) {
        return getAllBookingsSortedByBookingTime(null, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookingsSortedByBookingTime(LocalDateTime fromDate, Pageable pageable) {
        return getAllBookingsSortedByBookingTime(fromDate, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookingsSortedByBookingTime(LocalDateTime fromDate, BookingStatus status, Pageable pageable) {
        log.info("Admin fetching all bookings sorted by booking time descending with fromDate: {}, status: {}", fromDate, status);

        Page<Booking> allBookings;
        if (status != null && fromDate != null) {
            allBookings = bookingRepository.findAllBookingsOrderByBookingTimeDescWithDateAndStatus(status, fromDate, pageable);
        } else if (status != null) {
            allBookings = bookingRepository.findAllBookingsOrderByBookingTimeDescWithStatus(status, pageable);
        } else if (fromDate != null) {
            allBookings = bookingRepository.findAllBookingsOrderByBookingTimeDescWithDate(fromDate, pageable);
        } else {
            allBookings = bookingRepository.findAllBookingsOrderByBookingTimeDesc(pageable);
        }

        Page<BookingResponse> response = allBookings.map(bookingMapper::toBookingResponse);

        log.info("Found {} bookings (page {} of {})",
                allBookings.getNumberOfElements(),
                allBookings.getNumber() + 1,
                allBookings.getTotalPages());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookingsByBookingCode(String bookingCode, Pageable pageable) {
        log.info("Admin searching bookings by booking code: {}", bookingCode);

        Page<Booking> bookings = bookingRepository.searchBookingsByCode(bookingCode, pageable);
        Page<BookingResponse> response = bookings.map(bookingMapper::toBookingResponse);

        log.info("Found {} bookings matching code '{}' (page {} of {})",
                bookings.getNumberOfElements(),
                bookingCode,
                bookings.getNumber() + 1,
                bookings.getTotalPages());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByPaymentFlag(Boolean isPaid, BookingStatus status, Pageable pageable) {
        boolean targetPaid = Boolean.TRUE.equals(isPaid);
        log.info("Admin fetching bookings by payment flag. isPaid: {}, status: {}", targetPaid, status);

        Page<Booking> bookings = targetPaid
                ? bookingRepository.findPaidBookings(status, pageable)
                : bookingRepository.findUnpaidBookings(status, pageable);
        Page<BookingResponse> response = bookings.map(bookingMapper::toBookingResponse);

        log.info("Found {} bookings (page {} of {})",
                bookings.getNumberOfElements(),
                bookings.getNumber() + 1,
                bookings.getTotalPages());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByEmployeeId(String employeeId, Pageable pageable) {
        log.info("Employee {} fetching their assigned bookings", employeeId);

        Page<Booking> employeeBookings = bookingRepository.findBookingsByEmployeeIdOrderByBookingTime(employeeId, pageable);

        Page<BookingResponse> response = employeeBookings.map(bookingMapper::toBookingResponse);

        log.info("Found {} bookings for employee {} (page {} of {})",
                employeeBookings.getNumberOfElements(),
                employeeId,
                employeeBookings.getNumber() + 1,
                employeeBookings.getTotalPages());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByEmployeeId(String employeeId, LocalDateTime fromDate, Pageable pageable) {
        log.info("Employee {} fetching assigned bookings from date {}", employeeId, fromDate);

        Page<Booking> employeeBookings;
        if (fromDate != null) {
            employeeBookings = bookingRepository.findBookingsByEmployeeIdOrderByBookingTimeWithDate(employeeId, fromDate, pageable);
        } else {
            employeeBookings = bookingRepository.findBookingsByEmployeeIdOrderByBookingTime(employeeId, pageable);
        }

        Page<BookingResponse> response = employeeBookings.map(bookingMapper::toBookingResponse);

        log.info("Found {} bookings for employee {} from date {} (page {} of {})",
                employeeBookings.getNumberOfElements(),
                employeeId,
                fromDate,
                employeeBookings.getNumber() + 1,
                employeeBookings.getTotalPages());

        return response;
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(String bookingId, UpdateBookingStatusRequest request) {
        log.info("Admin updating booking {} status to {}", bookingId, request.getStatus());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking với ID: " + bookingId));

        // Update booking status
        booking.setStatus(request.getStatus());
        
        // Set isVerified to true when admin updates status
        booking.setIsVerified(true);
        
        // Save admin comment if provided
        if (request.getAdminComment() != null && !request.getAdminComment().trim().isEmpty()) {
            booking.setAdminComment(request.getAdminComment());
            log.info("Booking {} status updated to {} with admin comment", bookingId, request.getStatus());
        } else {
            log.info("Booking {} status updated to {} by admin", bookingId, request.getStatus());
        }

        Booking savedBooking = bookingRepository.save(booking);
        publishBookingStatusEvent(savedBooking, "ADMIN_UPDATE", request.getAdminComment());

        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public MultipleBookingCreationSummary createMultipleBookings(MultipleBookingCreateRequest request) {
        log.info("Creating multiple bookings for {} time slots", request.bookingTimes().size());

        // ========== OPTIMIZATION 1: Pre-validate and cache shared data ==========
        // Pre-load services, address, customer, promotion, fees - these are the same for all bookings
        MultipleBookingSharedContext sharedContext;
        try {
            sharedContext = preloadSharedContext(request);
        } catch (Exception e) {
            log.error("Failed to preload shared context: {}", e.getMessage());
            throw new BookingValidationException(List.of(e.getMessage()));
        }

        List<BookingCreationSummary> successfulBookings = Collections.synchronizedList(new ArrayList<>());
        List<MultipleBookingCreationSummary.BookingCreationError> errors = Collections.synchronizedList(new ArrayList<>());
        BigDecimal totalAmount = BigDecimal.ZERO;

        // ========== OPTIMIZATION 2: Parallel processing with controlled thread pool ==========
        // Use CompletableFuture for parallel booking creation with separate transactions
        List<CompletableFuture<BookingCreationResult>> futures = new ArrayList<>();
        
        for (int i = 0; i < request.bookingTimes().size(); i++) {
            final int index = i;
            final LocalDateTime bookingTime = request.bookingTimes().get(i);
            
            CompletableFuture<BookingCreationResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    log.debug("Creating booking {}/{} for time: {}", index + 1, request.bookingTimes().size(), bookingTime);
                    
                    // Create individual booking request for this time slot
                    BookingCreateRequest singleBookingRequest = new BookingCreateRequest(
                        request.addressId(),
                        request.newAddress(),
                        bookingTime,
                        request.note(),
                        request.title(),
                        request.imageUrls() != null ? request.imageUrls() : new ArrayList<>(),
                        request.promoCode(),
                        request.bookingDetails(),
                        request.assignments() != null ? request.assignments() : new ArrayList<>(),
                        request.paymentMethodId(),
                        request.additionalFeeIds()
                    );

                    // Use self-injection for new transaction per booking
                    BookingCreationSummary summary = self.createBookingWithNewTransaction(singleBookingRequest, sharedContext);
                    log.debug("Successfully created booking {} at index {}", summary.getBookingCode(), index);
                    return new BookingCreationResult(index, summary, null, null);
                    
                } catch (BookingValidationException e) {
                    log.debug("Validation error creating booking at index {}: {}", index, e.getMessage());
                    return new BookingCreationResult(index, null, buildValidationError(index, bookingTime, e), null);
                } catch (EmployeeConflictException e) {
                    log.debug("Employee conflict creating booking at index {}: {}", index, e.getMessage());
                    return new BookingCreationResult(index, null, buildConflictError(index, bookingTime, e), null);
                } catch (Exception e) {
                    log.warn("Unexpected error creating booking at index {}: {}", index, e.getMessage());
                    return new BookingCreationResult(index, null, buildUnexpectedError(index, bookingTime, e), null);
                }
            }, bookingExecutor);
            
            futures.add(future);
        }

        // Wait for all futures to complete and collect results
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        for (CompletableFuture<BookingCreationResult> future : futures) {
            try {
                BookingCreationResult result = future.get();
                if (result.summary() != null) {
                    successfulBookings.add(result.summary());
                } else if (result.error() != null) {
                    errors.add(result.error());
                }
            } catch (Exception e) {
                log.error("Error getting future result: {}", e.getMessage());
            }
        }

        // Calculate total amount (thread-safe since we're done with parallel processing)
        for (BookingCreationSummary s : successfulBookings) {
            totalAmount = totalAmount.add(s.getTotalAmount());
        }

        // Format total amount
        String formattedTotalAmount = String.format("%,.0f VND", totalAmount);

        MultipleBookingCreationSummary summary = MultipleBookingCreationSummary.builder()
            .totalBookingsCreated(request.bookingTimes().size())
            .successfulBookings(successfulBookings.size())
            .failedBookings(errors.size())
            .totalAmount(totalAmount)
            .formattedTotalAmount(formattedTotalAmount)
            .bookings(new ArrayList<>(successfulBookings))
            .errors(new ArrayList<>(errors))
            .build();

        log.info("Multiple booking creation completed: {}/{} successful, {}/{} failed",
            successfulBookings.size(), request.bookingTimes().size(),
            errors.size(), request.bookingTimes().size());

        return summary;
    }

    /**
     * Helper record for parallel booking creation result
     */
    private record BookingCreationResult(
        int index,
        BookingCreationSummary summary,
        MultipleBookingCreationSummary.BookingCreationError error,
        Exception exception
    ) {}

    /**
     * Pre-load and validate shared context for multiple bookings.
     * This avoids repeated DB queries for address, services, promotion, fees.
     */
    private MultipleBookingSharedContext preloadSharedContext(MultipleBookingCreateRequest request) {
        // Pre-validate address and customer
        Address address;
        Customer customer;
        if (request.addressId() != null && !request.addressId().isBlank()) {
            address = addressRepository.findAddressWithCustomer(request.addressId())
                    .orElseThrow(() -> AddressNotFoundException.withId(request.addressId()));
            customer = address.getCustomer();
            if (customer == null) {
                throw CustomerNotFoundException.forAddress(request.addressId());
            }
        } else if (request.newAddress() != null) {
            customer = customerRepository.findById(request.newAddress().customerId())
                    .orElseThrow(() -> CustomerNotFoundException.withId(request.newAddress().customerId()));
            address = null; // Will be created per booking
        } else {
            throw new IllegalArgumentException("Either addressId or newAddress must be provided");
        }

        // Pre-load all services in one query
        List<Integer> serviceIds = request.bookingDetails().stream()
                .map(BookingDetailRequest::serviceId)
                .distinct()
                .toList();
        Map<Integer, Service> servicesMap = serviceRepository.findAllById(serviceIds).stream()
                .collect(Collectors.toMap(Service::getServiceId, s -> s));
        
        // Validate all services exist and are active
        for (Integer serviceId : serviceIds) {
            Service service = servicesMap.get(serviceId);
            if (service == null) {
                throw ServiceNotFoundException.withId(serviceId);
            }
            if (!service.getIsActive()) {
                throw new BookingValidationException(List.of("Dịch vụ " + service.getName() + " không thể đặt"));
            }
        }

        // Pre-validate promotion (if provided)
        Promotion promotion = null;
        if (request.promoCode() != null && !request.promoCode().trim().isEmpty()) {
            promotion = promotionRepository.findByPromoCode(request.promoCode()).orElse(null);
        }

        // Pre-validate payment method
        iuh.house_keeping_service_be.models.PaymentMethod paymentMethod = paymentMethodRepository.findById(request.paymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found"));

        return new MultipleBookingSharedContext(
            address, customer, servicesMap, promotion, paymentMethod
        );
    }

    /**
     * Shared context for multiple booking creation to avoid repeated DB queries
     */
    private record MultipleBookingSharedContext(
        Address address,
        Customer customer,
        Map<Integer, Service> servicesMap,
        Promotion promotion,
        iuh.house_keeping_service_be.models.PaymentMethod paymentMethod
    ) {}

    /**
     * Create booking with new transaction (REQUIRES_NEW) for isolation.
     * Each booking has its own transaction so failures don't affect others.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookingCreationSummary createBookingWithNewTransaction(
            BookingCreateRequest request, 
            MultipleBookingSharedContext sharedContext) {
        // Simply delegate to the existing createBooking method
        // The sharedContext is currently not used directly here but can be leveraged
        // for further optimization in the future
        return createBooking(request);
    }

    private MultipleBookingCreationSummary.BookingCreationError buildValidationError(
            int index, LocalDateTime bookingTime, BookingValidationException e) {
        return MultipleBookingCreationSummary.BookingCreationError.builder()
            .index(index)
            .bookingTime(bookingTime.toString())
            .errorMessage("Lỗi xác thực booking")
            .details(e.getErrors())
            .build();
    }

    private MultipleBookingCreationSummary.BookingCreationError buildConflictError(
            int index, LocalDateTime bookingTime, EmployeeConflictException e) {
        List<String> conflictDetails = e.getConflicts().stream()
            .map(conflict -> String.format("%s - %s từ %s đến %s (ID: %s)",
                conflict.conflictType(),
                conflict.description(),
                conflict.startTime(),
                conflict.endTime(),
                conflict.conflictId()))
            .collect(Collectors.toList());
        
        return MultipleBookingCreationSummary.BookingCreationError.builder()
            .index(index)
            .bookingTime(bookingTime.toString())
            .errorMessage("Xung đột lịch nhân viên")
            .details(conflictDetails)
            .build();
    }

    private MultipleBookingCreationSummary.BookingCreationError buildUnexpectedError(
            int index, LocalDateTime bookingTime, Exception e) {
        return MultipleBookingCreationSummary.BookingCreationError.builder()
            .index(index)
            .bookingTime(bookingTime.toString())
            .errorMessage("Lỗi không xác định: " + e.getMessage())
            .details(List.of(e.getClass().getSimpleName()))
            .build();
    }

    @Override
    public BookingStatisticsByStatusResponse getBookingStatisticsByStatus(
            String customerId, String timeUnit, LocalDateTime startDate, LocalDateTime endDate) {
        
        log.info("Getting booking statistics for customer: {}, timeUnit: {}, from: {} to: {}", 
                 customerId, timeUnit, startDate, endDate);
        
        // Validate customer exists
        customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + customerId));
        
        // Calculate date range based on time unit if not provided
        LocalDateTime calculatedStartDate = startDate;
        LocalDateTime calculatedEndDate = endDate;
        
        if (startDate == null || endDate == null) {
            calculatedEndDate = LocalDateTime.now();
            
            switch (timeUnit.toUpperCase()) {
                case "DAY":
                    calculatedStartDate = calculatedEndDate.minusDays(1).withHour(0).withMinute(0).withSecond(0);
                    calculatedEndDate = calculatedEndDate.withHour(23).withMinute(59).withSecond(59);
                    break;
                case "WEEK":
                    calculatedStartDate = calculatedEndDate.minusWeeks(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "MONTH":
                    calculatedStartDate = calculatedEndDate.minusMonths(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                case "YEAR":
                    calculatedStartDate = calculatedEndDate.minusYears(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                default:
                    throw new IllegalArgumentException("Đơn vị thời gian không hợp lệ. Chỉ chấp nhận: DAY, WEEK, MONTH, YEAR");
            }
        }
        
        // Count bookings by each status
        Map<BookingStatus, Long> countByStatus = new HashMap<>();
        long totalBookings = 0;
        
        for (BookingStatus status : BookingStatus.values()) {
            long count = bookingRepository.countByCustomerIdAndStatusAndDateRange(
                customerId, status, calculatedStartDate, calculatedEndDate);
            countByStatus.put(status, count);
            totalBookings += count;
        }
        
        log.info("Statistics retrieved: {} total bookings for customer {}", totalBookings, customerId);
        
        return BookingStatisticsByStatusResponse.builder()
            .timeUnit(timeUnit.toUpperCase())
            .startDate(calculatedStartDate.toString())
            .endDate(calculatedEndDate.toString())
            .totalBookings(totalBookings)
            .countByStatus(countByStatus)
            .build();
    }

    private FeeCalculationResult calculateFees(BigDecimal baseAmount, List<String> selectedFeeIds) {
        BigDecimal safeBase = baseAmount != null ? baseAmount : BigDecimal.ZERO;
        List<FeeBreakdownInfo> breakdowns = new ArrayList<>();
        BigDecimal totalFees = BigDecimal.ZERO;

        AdditionalFee system = additionalFeeService.getActiveSystemSurcharge();
        if (system == null) {
            system = new AdditionalFee();
            system.setName("Phí hệ thống");
            system.setFeeType(AdditionalFeeType.PERCENT);
            system.setValue(new BigDecimal("0.20"));
            system.setSystemSurcharge(true);
            system.setActive(true);
        }

        List<AdditionalFee> fees = new ArrayList<>();
        fees.add(system);
        if (selectedFeeIds != null && !selectedFeeIds.isEmpty()) {
            fees.addAll(additionalFeeService.getActiveFeesByIds(selectedFeeIds));
        }

        for (AdditionalFee fee : fees) {
            if (!fee.isActive()) continue;
            BigDecimal amount = calculateFeeAmount(safeBase, fee.getFeeType(), fee.getValue());
            totalFees = totalFees.add(amount);
            breakdowns.add(FeeBreakdownInfo.builder()
                    .name(fee.getName())
                    .type(fee.getFeeType())
                    .value(fee.getValue())
                    .amount(amount)
                    .systemSurcharge(fee.isSystemSurcharge())
                    .build());
        }

        return new FeeCalculationResult(totalFees, breakdowns);
    }

    private BigDecimal calculateFeeAmount(BigDecimal base, AdditionalFeeType type, BigDecimal value) {
        if (type == AdditionalFeeType.FLAT) {
            return value.setScale(0, RoundingMode.HALF_UP);
        }
        return base.multiply(value).setScale(0, RoundingMode.HALF_UP);
    }

    private record FeeCalculationResult(BigDecimal totalFees, List<FeeBreakdownInfo> breakdowns) {
    }
}
