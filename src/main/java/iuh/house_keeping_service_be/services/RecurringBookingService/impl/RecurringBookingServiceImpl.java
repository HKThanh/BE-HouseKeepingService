package iuh.house_keeping_service_be.services.RecurringBookingService.impl;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingDetailRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingCancelRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingCreateRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingDetailRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingCreationSummary;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingResponse;
import iuh.house_keeping_service_be.dtos.Chat.ConversationRequest;
import iuh.house_keeping_service_be.dtos.Chat.ConversationResponse;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeRequest;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeResponse;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.ApiResponse;
import iuh.house_keeping_service_be.dtos.Booking.request.AssignmentRequest;
import iuh.house_keeping_service_be.events.RecurringBookingCreatedEvent;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.enums.RecurrenceType;
import iuh.house_keeping_service_be.enums.RecurringBookingStatus;
import iuh.house_keeping_service_be.exceptions.*;
import iuh.house_keeping_service_be.mappers.RecurringBookingMapper;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.ChatService.ConversationService;
import iuh.house_keeping_service_be.services.EmployeeScheduleService.EmployeeScheduleService;
import iuh.house_keeping_service_be.services.RecurringBookingService.RecurringBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringBookingServiceImpl implements RecurringBookingService {

    // Giới hạn đồng bộ nhỏ để phản hồi nhanh, tránh 504
    private static final int MAX_INITIAL_GENERATION = 3;
    private static final int DEFAULT_GENERATION_WINDOW_DAYS = 7;
    private static final int ASSIGNED_GENERATION_WINDOW_DAYS = 7;
    private static final int MAX_TOTAL_OCCURRENCES = 365;

    private final RecurringBookingRepository recurringBookingRepository;
    private final RecurringBookingDetailRepository recurringBookingDetailRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ServiceRepository serviceRepository;
    private final PromotionRepository promotionRepository;
    private final BookingRepository bookingRepository;
    private final RecurringBookingMapper recurringBookingMapper;
    private final BookingService bookingService;
    private final ConversationService conversationService;
    private final AssignmentRepository assignmentRepository;
    private final EmployeeScheduleService employeeScheduleService;
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;
    private final ConversationRepository conversationRepository;
    @Lazy
    @Autowired
    private RecurringBookingServiceImpl self;

    @Override
    @Transactional
    public RecurringBookingCreationSummary createRecurringBooking(RecurringBookingCreateRequest request, String customerId) {
        log.info("Creating recurring booking for customer: {}", customerId);

        try {
            // Validate customer
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> CustomerNotFoundException.withId(customerId));

            // Validate and get address
            Address address = validateAndGetAddress(request, customer);

            // Validate recurrence timeline rules (dates, recurrence days, limits)
            List<Integer> normalizedRecurrenceDays = validateAndNormalizeTimeRules(request);

            // Validate services
            List<Service> services = validateServices(request.bookingDetails());

            // Create recurring booking entity
            RecurringBooking recurringBooking = createRecurringBookingEntity(
                    request,
                    customer,
                    address,
                    normalizedRecurrenceDays
            );

            // Set promotion if applicable
            if (request.promoCode() != null && !request.promoCode().trim().isEmpty()) {
                promotionRepository.findByPromoCode(request.promoCode())
                        .ifPresent(recurringBooking::setPromotion);
            }

            // ========== SYNCHRONOUS EMPLOYEE ASSIGNMENT ==========
            // Calculate planned booking times for the generation window
            int windowDays = ASSIGNED_GENERATION_WINDOW_DAYS;
            LocalDate windowStart = resolveWindowStart(request.startDate(), LocalDate.now());
            LocalDate windowEnd = resolveWindowEnd(windowStart, request.endDate(), windowDays);
            
            // Create a temporary RecurringBooking to calculate times (using request data)
            List<LocalDateTime> plannedBookingTimes = calculatePlannedBookingTimesFromRequest(
                    request, 
                    normalizedRecurrenceDays, 
                    windowStart, 
                    windowEnd
            );
            
            log.info("Calculated {} planned booking times in window for employee assignment", plannedBookingTimes.size());
            
            // Find suitable employee for all planned time slots
            Employee assignedEmployee = null;
            List<AssignmentRequest> recurringAssignments = new ArrayList<>();
            
            if (!plannedBookingTimes.isEmpty() && !request.bookingDetails().isEmpty()) {
                assignedEmployee = findSuitableEmployeeForRecurringBookings(
                        request.bookingDetails(),
                        plannedBookingTimes,
                        address,
                        customerId
                );
                
                if (assignedEmployee != null) {
                    log.info("Found suitable employee {} for recurring booking", assignedEmployee.getFullName());
                    recurringBooking.setAssignedEmployee(assignedEmployee);
                    
                    // Build assignment requests for each service
                    for (RecurringBookingDetailRequest detailRequest : request.bookingDetails()) {
                        recurringAssignments.add(new AssignmentRequest(
                                assignedEmployee.getEmployeeId(), 
                                detailRequest.serviceId()
                        ));
                    }
                } else {
                    log.warn("No suitable employee found for recurring booking - will use auto-assign per booking");
                }
            }
            // ========== END SYNCHRONOUS EMPLOYEE ASSIGNMENT ==========

            // Save recurring booking (with assigned employee if found)
            RecurringBooking savedRecurringBooking = recurringBookingRepository.save(recurringBooking);

            // Create recurring booking details
            List<RecurringBookingDetail> details = createRecurringBookingDetails(
                    savedRecurringBooking, 
                    request.bookingDetails(), 
                    services
            );
            List<RecurringBookingDetail> savedDetails = recurringBookingDetailRepository.saveAll(details);
            
            // Manually add each detail to the collection to ensure it's loaded in memory
            // Do NOT use setRecurringBookingDetails() as it may trigger unwanted updates
            savedRecurringBooking.getRecurringBookingDetails().clear();
            savedRecurringBooking.getRecurringBookingDetails().addAll(savedDetails);
            
            log.info("Loaded {} recurring booking details into memory", savedDetails.size());

            // ========== GENERATE INITIAL BOOKINGS WITH ASSIGNED EMPLOYEE ==========
            List<String> generatedBookingIds = new ArrayList<>();
            if (assignedEmployee != null && !plannedBookingTimes.isEmpty()) {
                // Limit initial generation to avoid timeout
                List<LocalDateTime> initialTimes = plannedBookingTimes.stream()
                        .filter(dt -> dt.isAfter(LocalDateTime.now()))
                        .sorted()
                        .limit(MAX_INITIAL_GENERATION)
                        .toList();
                
                for (LocalDateTime bookingTime : initialTimes) {
                    try {
                        String bookingId = createBookingFromRecurring(
                                savedRecurringBooking, 
                                bookingTime, 
                                recurringAssignments
                        );
                        if (bookingId != null) {
                            generatedBookingIds.add(bookingId);
                        }
                    } catch (Exception e) {
                        log.error("Error creating initial booking at {}: {}", bookingTime, e.getMessage());
                    }
                }
                log.info("Generated {} initial bookings with assigned employee", generatedBookingIds.size());
            }
            // ========== END GENERATE INITIAL BOOKINGS ==========

            // Enqueue background tasks for remaining bookings via event
            eventPublisher.publishEvent(new RecurringBookingCreatedEvent(
                    savedRecurringBooking.getRecurringBookingId(),
                    request.bookingDetails()
            ));

            log.info("Recurring booking created successfully with ID: {}", savedRecurringBooking.getRecurringBookingId());

            // Build response (mapper handles assignedEmployee info)
            RecurringBookingResponse response = recurringBookingMapper.toResponse(savedRecurringBooking);
            
            // Không chờ sinh booking, chỉ tính nhanh kỳ vọng trong cửa sổ (không scan DB để tránh chậm)
            populateInitialExpectedStats(response, request, normalizedRecurrenceDays);
            
            // Update with actual generated bookings count
            int generatedCount = generatedBookingIds.size();
            response.setTotalGeneratedBookings(generatedCount);
            response.setUpcomingBookings(generatedCount); // All newly created bookings are upcoming
            response.setGeneratedBookingsInWindow(generatedCount);
            if (response.getExpectedBookingsInWindow() > 0) {
                response.setGenerationProgressPercent(
                        (generatedCount * 100.0) / response.getExpectedBookingsInWindow()
                );
            }

            ConversationResponse conversation = createRecurringConversation(savedRecurringBooking, generatedBookingIds);

            RecurringBookingCreationSummary summary = new RecurringBookingCreationSummary();
            summary.setSuccess(true);
            summary.setMessage("Đặt lịch định kỳ thành công");
            summary.setRecurringBooking(response);
            summary.setGeneratedBookingIds(generatedBookingIds);
            summary.setConversation(conversation);
            summary.setTotalBookingsToBeCreated(response.getExpectedBookingsInWindow());
            summary.setExpectedBookingsInWindow(response.getExpectedBookingsInWindow());
            summary.setGeneratedBookingsInWindow(generatedBookingIds.size());
            summary.setGenerationWindowDays(response.getGenerationWindowDays());
            summary.setGenerationProgressPercent(response.getGenerationProgressPercent());

            return summary;

        } catch (CustomerNotFoundException | AddressNotFoundException | ServiceNotFoundException e) {
            log.error("Validation error creating recurring booking: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating recurring booking: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo lịch định kỳ: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate planned booking times from request data (before RecurringBooking is saved)
     */
    private List<LocalDateTime> calculatePlannedBookingTimesFromRequest(
            RecurringBookingCreateRequest request,
            List<Integer> normalizedRecurrenceDays,
            LocalDate windowStart,
            LocalDate windowEnd
    ) {
        List<LocalDateTime> times = new ArrayList<>();
        
        LocalDate currentDate = windowStart.isAfter(request.startDate()) 
                ? windowStart 
                : request.startDate();
        
        while (!currentDate.isAfter(windowEnd)) {
            if (request.endDate() != null && currentDate.isAfter(request.endDate())) {
                break;
            }
            if (shouldGenerateBookingForDate(request.recurrenceType(), normalizedRecurrenceDays, currentDate)) {
                LocalDateTime bookingDateTime = LocalDateTime.of(currentDate, request.bookingTime());
                if (bookingDateTime.isAfter(LocalDateTime.now())) {
                    times.add(bookingDateTime);
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return times.stream().sorted().toList();
    }

    /**
     * Find a suitable employee who can work ALL the specified booking times.
     * This method searches for an employee available at every time slot in the list.
     */
    private Employee findSuitableEmployeeForRecurringBookings(
            List<RecurringBookingDetailRequest> detailRequests,
            List<LocalDateTime> bookingTimes,
            Address address,
            String customerId
    ) {
        if (detailRequests == null || detailRequests.isEmpty() || bookingTimes == null || bookingTimes.isEmpty()) {
            return null;
        }

        String ward = address != null ? address.getWard() : null;
        String city = address != null ? address.getCity() : null;

        // Get the first service to find suitable employees
        // (We assume the same employee can handle all services in the recurring booking)
        Integer serviceId = detailRequests.get(0).serviceId();

        // Create request with all booking times
        SuitableEmployeeRequest suitableRequest = new SuitableEmployeeRequest(
                serviceId,
                null,  // bookingTime - we use bookingTimes instead
                ward,
                city,
                customerId,
                bookingTimes  // Check availability for ALL these times
        );

        try {
            ApiResponse<List<SuitableEmployeeResponse>> response = employeeScheduleService.findSuitableEmployees(suitableRequest);
            
            if (response != null && response.success() && response.data() != null && !response.data().isEmpty()) {
                // Get the first (best-ranked) employee
                SuitableEmployeeResponse selectedEmployee = response.data().get(0);
                
                // Fetch the full Employee entity
                return employeeRepository.findById(selectedEmployee.employeeId()).orElse(null);
            }
            
            log.warn("No employee found available for all {} time slots", bookingTimes.size());
            return null;
            
        } catch (Exception e) {
            log.error("Error finding suitable employee for recurring bookings: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public RecurringBookingResponse cancelRecurringBooking(
            String recurringBookingId,
            String customerId,
            RecurringBookingCancelRequest request
    ) {
        log.info("Cancelling recurring booking: {} for customer: {}", recurringBookingId, customerId);

        try {
            // Find recurring booking
            RecurringBooking recurringBooking = recurringBookingRepository
                    .findByRecurringBookingIdAndCustomer_CustomerId(recurringBookingId, customerId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch định kỳ"));

            // Check if already cancelled
            if (recurringBooking.getStatus() == RecurringBookingStatus.CANCELLED) {
                throw new RuntimeException("Lịch định kỳ đã được hủy trước đó");
            }

            // Update recurring booking status
            recurringBooking.setStatus(RecurringBookingStatus.CANCELLED);
            recurringBooking.setCancelledAt(LocalDateTime.now());
            recurringBooking.setCancellationReason(request.reason());

            // Find all future bookings (bookings with status PENDING or AWAITING_EMPLOYEE)
            LocalDateTime now = LocalDateTime.now();
            List<BookingStatus> cancellableStatuses = List.of(BookingStatus.PENDING, BookingStatus.AWAITING_EMPLOYEE);
            List<Booking> futureBookings = bookingRepository.findFutureByRecurringAndStatuses(
                    recurringBookingId,
                    now,
                    cancellableStatuses
            );

            if (!futureBookings.isEmpty()) {
                List<String> bookingIds = futureBookings.stream()
                        .map(Booking::getBookingId)
                        .collect(Collectors.toList());

                log.info("Deleting {} future bookings and related records", futureBookings.size());

                // Step 1: Delete related records in correct order to avoid FK constraint violations
                // 1.1 Delete assignments (references booking_details)
                assignmentRepository.deleteByBookingIds(bookingIds);
                log.debug("Deleted assignments for bookings: {}", bookingIds);

                // 1.2 Unlink conversations from bookings (set booking_id = null, don't delete conversations)
                conversationRepository.unlinkBookingsByIds(bookingIds);
                log.debug("Unlinked conversations for bookings: {}", bookingIds);

                // 1.3 Delete payments (references bookings)
                paymentRepository.deleteByBookingIds(bookingIds);
                log.debug("Deleted payments for bookings: {}", bookingIds);

                // Step 2: Delete bookings (cascade will delete booking_details, additional_fees, image_urls)
                bookingRepository.deleteAllInBatch(futureBookings);
                log.debug("Deleted bookings: {}", bookingIds);
            }

            RecurringBooking saved = recurringBookingRepository.save(recurringBooking);

            log.info("Recurring booking cancelled successfully: {}", recurringBookingId);

            return recurringBookingMapper.toResponse(saved);

        } catch (Exception e) {
            log.error("Error cancelling recurring booking: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể hủy lịch định kỳ: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<RecurringBookingResponse> getRecurringBookingsByCustomer(String customerId, Pageable pageable) {
        log.info("Getting recurring bookings for customer: {}", customerId);

        Page<RecurringBooking> recurringBookings = recurringBookingRepository
                .findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId, pageable);

        return recurringBookings.map(rb -> {
            RecurringBookingResponse response = recurringBookingMapper.toResponse(rb);
            updateStatistics(response, rb);
            return response;
        });
    }

    @Override
    public RecurringBookingResponse getRecurringBookingDetails(String recurringBookingId, String customerId) {
        log.info("Getting recurring booking details: {} for customer: {}", recurringBookingId, customerId);

        RecurringBooking recurringBooking = recurringBookingRepository
                .findByRecurringBookingIdAndCustomer_CustomerId(recurringBookingId, customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch định kỳ"));

        RecurringBookingResponse response = recurringBookingMapper.toResponse(recurringBooking);
        updateStatistics(response, recurringBooking);
        return response;
    }

    @Override
    @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM every day
    @Transactional
    public void generateBookingsForActiveRecurringBookings() {
        log.info("Starting scheduled job to generate bookings for active recurring bookings");

        try {
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(30); // Generate bookings for next 30 days

            List<RecurringBooking> activeRecurringBookings = recurringBookingRepository
                    .findActiveRecurringBookingsForGeneration(RecurringBookingStatus.ACTIVE, today);

            log.info("Found {} active recurring bookings", activeRecurringBookings.size());

            for (RecurringBooking recurringBooking : activeRecurringBookings) {
                try {
                    Set<LocalDateTime> existingTimes = getExistingBookingTimes(
                            recurringBooking.getRecurringBookingId(),
                            today,
                            futureDate
                    );

                    List<LocalDateTime> plannedBookingTimes = collectPlannedBookingTimes(
                            recurringBooking,
                            today,
                            futureDate,
                            existingTimes
                    ).stream().sorted().toList();

                    if (plannedBookingTimes.isEmpty()) {
                        continue;
                    }

                    List<LocalDateTime> assignmentWindow = plannedBookingTimes.stream()
                            .limit(MAX_INITIAL_GENERATION)
                            .toList();

                    List<RecurringBookingDetailRequest> detailRequests = recurringBooking.getRecurringBookingDetails().stream()
                            .map(d -> new RecurringBookingDetailRequest(
                                    d.getService().getServiceId(),
                                    d.getQuantity(),
                                    d.getPricePerUnit() != null && d.getQuantity() != null
                                            ? d.getPricePerUnit().multiply(BigDecimal.valueOf(d.getQuantity()))
                                            : null,
                                    d.getPricePerUnit(),
                                    parseChoiceIds(d.getSelectedChoiceIds())
                            ))
                            .toList();

                    List<AssignmentRequest> recurringAssignments = buildRecurringAssignments(
                            recurringBooking, assignmentWindow, detailRequests);

                    List<String> generatedIds = generateBookingsForTimes(
                            recurringBooking,
                            plannedBookingTimes,
                            recurringAssignments,
                            existingTimes
                    );
                    if (!generatedIds.isEmpty()) {
                        createRecurringConversation(recurringBooking, generatedIds);
                    }
                } catch (Exception e) {
                    log.error("Error generating bookings for recurring booking {}: {}", 
                            recurringBooking.getRecurringBookingId(), e.getMessage(), e);
                }
            }

            log.info("Completed generating bookings for active recurring bookings");

        } catch (Exception e) {
            log.error("Error in scheduled job: {}", e.getMessage(), e);
        }
    }

    // Private helper methods

    private Address validateAndGetAddress(RecurringBookingCreateRequest request, Customer customer) {
        if (request.addressId() != null && !request.addressId().isBlank()) {
            Address address = addressRepository.findById(request.addressId())
                    .orElseThrow(() -> AddressNotFoundException.withId(request.addressId()));

            if (!address.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                throw new RuntimeException("Địa chỉ không thuộc về khách hàng này");
            }

            return address;
        } else if (request.newAddress() != null) {
            Address address = new Address();
            address.setCustomer(customer);
            address.setFullAddress(request.newAddress().fullAddress());
            address.setWard(request.newAddress().ward());
            address.setCity(request.newAddress().city());
            address.setLatitude(request.newAddress().latitude() != null 
                    ? BigDecimal.valueOf(request.newAddress().latitude()) : null);
            address.setLongitude(request.newAddress().longitude() != null 
                    ? BigDecimal.valueOf(request.newAddress().longitude()) : null);
            address.setIsDefault(Boolean.FALSE);

            return addressRepository.save(address);
        } else {
            throw new RuntimeException("Địa chỉ là bắt buộc");
        }
    }

    private List<Integer> validateAndNormalizeTimeRules(RecurringBookingCreateRequest request) {
        List<String> errors = new ArrayList<>();
        List<Integer> normalizedDays = new ArrayList<>();

        if (request.recurrenceType() == null) {
            errors.add("RECURRENCE_TYPE_REQUIRED: Loại lặp lại là bắt buộc");
        }

        if (request.bookingTime() == null) {
            errors.add("BOOKING_TIME_REQUIRED: Giờ đặt là bắt buộc");
        }

        if (request.recurrenceDays() == null || request.recurrenceDays().isEmpty()) {
            errors.add("RECURRENCE_DAYS_MISSING: Ngày lặp lại là bắt buộc");
        } else {
            normalizedDays = request.recurrenceDays().stream()
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();

            if (normalizedDays.isEmpty()) {
                errors.add("RECURRENCE_DAYS_INVALID: Ngày lặp lại không hợp lệ");
            }

            for (Integer day : normalizedDays) {
                if (request.recurrenceType() == RecurrenceType.WEEKLY) {
                    if (day < 1 || day > 7) {
                        errors.add("RECURRENCE_DAYS_WEEKLY_INVALID: Ngày trong tuần phải từ 1 (Thứ 2) đến 7 (Chủ nhật)");
                    }
                } else if (request.recurrenceType() == RecurrenceType.MONTHLY) {
                    if (day < 1 || day > 31) {
                        errors.add("RECURRENCE_DAYS_MONTHLY_INVALID: Ngày trong tháng phải từ 1 đến 31");
                    }
                }
            }
        }

        LocalDate today = LocalDate.now();
        if (request.startDate() == null) {
            errors.add("START_DATE_REQUIRED: Ngày bắt đầu là bắt buộc");
        } else if (request.startDate().isBefore(today)) {
            errors.add("START_DATE_BEFORE_TODAY: Ngày bắt đầu phải từ hôm nay trở đi");
        }

        if (request.endDate() != null && request.startDate() != null && request.endDate().isBefore(request.startDate())) {
            errors.add("END_DATE_BEFORE_START: Ngày kết thúc phải sau ngày bắt đầu");
        }

        LocalDate effectiveEnd = resolveEffectiveEndDate(request.startDate(), request.endDate());
        if (request.startDate() != null
                && effectiveEnd != null
                && !normalizedDays.isEmpty()
                && request.recurrenceType() != null) {
            int occurrences = countOccurrences(
                    request.recurrenceType(),
                    normalizedDays,
                    request.startDate(),
                    effectiveEnd
            );
            if (occurrences > MAX_TOTAL_OCCURRENCES) {
                errors.add(String.format(
                        "OCCURRENCE_LIMIT_EXCEEDED: Lịch định kỳ tạo quá %d lần trong khoảng thời gian cho phép",
                        MAX_TOTAL_OCCURRENCES
                ));
            }
        }

        if (!errors.isEmpty()) {
            throw RecurringBookingValidationException.timeRuleViolation(errors);
        }

        return normalizedDays;
    }

    private LocalDate resolveEffectiveEndDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            return endDate;
        }
        if (endDate != null) {
            return endDate;
        }
        return startDate.plusMonths(12);
    }

    private List<Service> validateServices(List<RecurringBookingDetailRequest> detailRequests) {
        // OPTIMIZATION: Batch load all services in one query instead of N queries
        List<Integer> serviceIds = detailRequests.stream()
                .map(RecurringBookingDetailRequest::serviceId)
                .toList();
        
        // Load all services in a single query
        Map<Integer, Service> servicesMap = serviceRepository.findAllById(serviceIds).stream()
                .collect(Collectors.toMap(Service::getServiceId, s -> s));
        
        List<Service> services = new ArrayList<>();
        
        for (RecurringBookingDetailRequest detail : detailRequests) {
            Service service = servicesMap.get(detail.serviceId());
            if (service == null) {
                throw ServiceNotFoundException.withId(detail.serviceId());
            }
            if (!service.getIsActive()) {
                throw new RuntimeException("Dịch vụ " + service.getName() + " không thể đặt");
            }
            services.add(service);
        }

        return services;
    }

    private RecurringBooking createRecurringBookingEntity(
            RecurringBookingCreateRequest request,
            Customer customer,
            Address address,
            List<Integer> normalizedRecurrenceDays
    ) {
        RecurringBooking recurringBooking = new RecurringBooking();

        recurringBooking.setCustomer(customer);
        recurringBooking.setAddress(address);
        recurringBooking.setAssignedEmployee(null);
        recurringBooking.setRecurrenceType(request.recurrenceType());
        recurringBooking.setRecurrenceDays(
                normalizedRecurrenceDays.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );
        recurringBooking.setBookingTime(request.bookingTime());
        recurringBooking.setStartDate(request.startDate());
        recurringBooking.setEndDate(request.endDate());
        recurringBooking.setNote(request.note());
        recurringBooking.setTitle(request.title());
        recurringBooking.setStatus(RecurringBookingStatus.ACTIVE);

        return recurringBooking;
    }

    private List<RecurringBookingDetail> createRecurringBookingDetails(
            RecurringBooking recurringBooking,
            List<RecurringBookingDetailRequest> detailRequests,
            List<Service> services
    ) {
        List<RecurringBookingDetail> details = new ArrayList<>();

        for (int i = 0; i < detailRequests.size(); i++) {
            RecurringBookingDetailRequest detailRequest = detailRequests.get(i);
            Service service = services.get(i);

            RecurringBookingDetail detail = new RecurringBookingDetail();
            detail.setRecurringBooking(recurringBooking);
            detail.setService(service);
            detail.setQuantity(detailRequest.quantity());
            
            // Calculate price per unit if not provided
            BigDecimal pricePerUnit = detailRequest.expectedPricePerUnit();
            if (pricePerUnit == null) {
                pricePerUnit = service.getBasePrice();
                log.debug("Auto-calculated pricePerUnit for service {}: {}", service.getServiceId(), pricePerUnit);
            }
            detail.setPricePerUnit(pricePerUnit);

            if (detailRequest.selectedChoiceIds() != null && !detailRequest.selectedChoiceIds().isEmpty()) {
                detail.setSelectedChoiceIds(
                        detailRequest.selectedChoiceIds().stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","))
                );
            }

            details.add(detail);
        }

        return details;
    }

    private ConversationResponse createRecurringConversation(RecurringBooking recurringBooking, List<String> generatedBookingIds) {
        if (recurringBooking == null || recurringBooking.getCustomer() == null) {
            return null;
        }

        try {
            // Prefer an already assigned employee on the recurring booking
            String pickedEmployeeId = recurringBooking.getAssignedEmployee() != null
                    ? recurringBooking.getAssignedEmployee().getEmployeeId()
                    : null;

            // If none, try to resolve an employee from generated bookings' assignments (first non-cancelled)
            if (pickedEmployeeId == null && generatedBookingIds != null) {
                for (String bookingId : generatedBookingIds) {
                    List<Assignment> assignments = assignmentRepository.findByBookingDetail_Booking_BookingId(bookingId);
                    if (assignments == null || assignments.isEmpty()) {
                        continue;
                    }
                    for (Assignment assignment : assignments) {
                        if (assignment.getEmployee() != null && assignment.getEmployee().getEmployeeId() != null) {
                            pickedEmployeeId = assignment.getEmployee().getEmployeeId();
                            break;
                        }
                    }
                    if (pickedEmployeeId != null) {
                        break;
                    }
                }
            }

            if (pickedEmployeeId == null) {
                log.warn("Could not resolve employee for recurring conversation {}", recurringBooking.getRecurringBookingId());
                return null;
            }

            ConversationRequest conversationRequest = new ConversationRequest(
                    recurringBooking.getCustomer().getCustomerId(),
                    pickedEmployeeId,
                    null,
                    recurringBooking.getRecurringBookingId()
            );

            return conversationService.createConversation(conversationRequest);
        } catch (Exception e) {
            log.error("Failed to create recurring conversation for {}: {}", recurringBooking.getRecurringBookingId(), e.getMessage());
            return null;
        }
    }

    @Async
    public void assignEmployeeAsync(String recurringBookingId, List<RecurringBookingDetailRequest> detailRequests) {
        if (recurringBookingId == null || detailRequests == null || detailRequests.isEmpty()) {
            return;
        }

        try {
            RecurringBooking recurringBooking = recurringBookingRepository.findByIdWithDetails(recurringBookingId)
                    .orElse(null);
            if (recurringBooking == null) {
                return;
            }

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(30);
            Set<LocalDateTime> existing = getExistingBookingTimes(recurringBookingId, startDate, endDate);
            List<LocalDateTime> plannedBookingTimes = collectPlannedBookingTimes(
                    recurringBooking,
                    startDate,
                    endDate,
                    existing
            );

            if (plannedBookingTimes.isEmpty()) {
                return;
            }

            // Chỉ cần 1 nhân viên phù hợp cho tất cả slot -> chọn người đầu tiên
            List<LocalDateTime> assignmentWindow = plannedBookingTimes.stream().limit(10).toList();

            List<AssignmentRequest> assignments = buildRecurringAssignments(recurringBooking, assignmentWindow, detailRequests);
            if (assignments.isEmpty()) {
                log.warn("No suitable employee found for recurring booking {}", recurringBookingId);
                return;
            }

            String employeeId = assignments.get(0).employeeId();
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee == null) {
                return;
            }

            recurringBooking.setAssignedEmployee(employee);
            recurringBookingRepository.save(recurringBooking);
            log.info("Assigned employee {} to recurring booking {}", employeeId, recurringBookingId);

            // Create or reuse conversation for this recurring booking once employee is known
            ConversationResponse conversation = createRecurringConversation(recurringBooking, List.of());
            if (conversation != null) {
                log.info("Created recurring conversation {} for {}", conversation.getConversationId(), recurringBookingId);
            }
        } catch (Exception e) {
            log.error("Failed to assign employee for recurring booking {}: {}", recurringBookingId, e.getMessage());
        }
    }

    // Tạo booking cho tuần tiếp theo (chạy 5h sáng thứ 2 hàng tuần)
    @Scheduled(cron = "0 0 5 ? * MON")
    @Transactional
    public void generateWeeklyBookings() {
        log.info("Starting weekly generation for recurring bookings (next 7 days)");
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        List<RecurringBooking> active = recurringBookingRepository
                .findActiveRecurringBookingsForGeneration(RecurringBookingStatus.ACTIVE, today);

        for (RecurringBooking rb : active) {
            try {
                if (rb.getAssignedEmployee() == null) {
                    continue; // chưa có nhân viên phù hợp
                }

                Set<LocalDateTime> existingTimes = getExistingBookingTimes(
                        rb.getRecurringBookingId(),
                        today,
                        endDate
                );

                List<LocalDateTime> plannedTimes = collectPlannedBookingTimes(
                        rb,
                        today,
                        endDate,
                        existingTimes
                ).stream().sorted().toList();

                if (plannedTimes.isEmpty()) {
                    continue;
                }

                List<RecurringBookingDetailRequest> detailRequests = rb.getRecurringBookingDetails().stream()
                        .map(d -> new RecurringBookingDetailRequest(
                                d.getService().getServiceId(),
                                d.getQuantity(),
                                d.getPricePerUnit() != null && d.getQuantity() != null
                                        ? d.getPricePerUnit().multiply(BigDecimal.valueOf(d.getQuantity()))
                                        : null,
                                d.getPricePerUnit(),
                                parseChoiceIds(d.getSelectedChoiceIds())
                        ))
                        .toList();

                List<AssignmentRequest> assignments = detailRequests.stream()
                        .map(dr -> new AssignmentRequest(rb.getAssignedEmployee().getEmployeeId(), dr.serviceId()))
                        .toList();

                List<String> generatedIds = generateBookingsForTimes(rb, plannedTimes, assignments, existingTimes);
                if (!generatedIds.isEmpty()) {
                    createRecurringConversation(rb, generatedIds);
                }
            } catch (Exception e) {
                log.error("Weekly generation failed for recurring {}: {}", rb.getRecurringBookingId(), e.getMessage());
            }
        }
        log.info("Weekly generation completed");
    }

    private List<String> generateBookingsForTimes(
            RecurringBooking recurringBooking,
            List<LocalDateTime> bookingTimes,
            List<AssignmentRequest> recurringAssignments,
            Set<LocalDateTime> existingTimes
    ) {
        if (bookingTimes == null || bookingTimes.isEmpty()) {
            return List.of();
        }

        Set<LocalDateTime> cache = existingTimes != null ? existingTimes : new HashSet<>();
        List<String> generatedBookingIds = new ArrayList<>();

        for (LocalDateTime bookingTime : bookingTimes.stream().sorted().toList()) {
            if (cache.contains(bookingTime)) {
                continue;
            }

            try {
                String bookingId = createBookingFromRecurring(recurringBooking, bookingTime, recurringAssignments);
                if (bookingId != null) {
                    generatedBookingIds.add(bookingId);
                    cache.add(bookingTime);
                }
            } catch (Exception e) {
                log.error("Error creating booking for recurring {} at {}: {}", recurringBooking.getRecurringBookingId(), bookingTime, e.getMessage());
            }
        }

        return generatedBookingIds;
    }

    @Async
    public void generateBookingsAsync(
            String recurringBookingId,
            List<LocalDateTime> bookingTimes,
            List<AssignmentRequest> recurringAssignments
    ) {
        if (bookingTimes == null || bookingTimes.isEmpty() || recurringBookingId == null) {
            return;
        }

        try {
            RecurringBooking recurringBooking = recurringBookingRepository
                    .findByIdWithDetails(recurringBookingId)
                    .orElseThrow(() -> new RuntimeException("Recurring booking not found: " + recurringBookingId));

            LocalDate minDate = bookingTimes.stream().map(LocalDateTime::toLocalDate).min(LocalDate::compareTo).orElse(LocalDate.now());
            LocalDate maxDate = bookingTimes.stream().map(LocalDateTime::toLocalDate).max(LocalDate::compareTo).orElse(minDate);

            Set<LocalDateTime> existingTimes = getExistingBookingTimes(recurringBookingId, minDate, maxDate);

            List<String> generatedIds = generateBookingsForTimes(
                    recurringBooking,
                    bookingTimes,
                    recurringAssignments,
                    existingTimes
            );
            if (!generatedIds.isEmpty()) {
                createRecurringConversation(recurringBooking, generatedIds);
            }
        } catch (Exception e) {
            log.error("Async generation failed for recurring booking {}: {}", recurringBookingId, e.getMessage());
        }
    }

    @Async
    @org.springframework.transaction.event.TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void handleRecurringBookingCreatedEvent(RecurringBookingCreatedEvent event) {
        try {
            RecurringBooking recurringBooking = recurringBookingRepository
                    .findByIdWithDetails(event.getRecurringBookingId())
                    .orElseGet(() -> recurringBookingRepository.findById(event.getRecurringBookingId()).orElse(null));
            if (recurringBooking == null) {
                log.warn("Recurring booking {} not found for background tasks", event.getRecurringBookingId());
                return;
            }
            List<RecurringBookingDetailRequest> detailRequests = event.getDetailRequests();
            self.assignEmployeeAsync(recurringBooking.getRecurringBookingId(), detailRequests);
            // Skip immediate slot generation to keep response fast; rely on scheduled jobs to generate bookings
        } catch (Exception e) {
            log.error("Failed to handle RecurringBookingCreatedEvent for {}: {}", event.getRecurringBookingId(), e.getMessage(), e);
        }
    }

    @Async
    public void generateInitialWindowAsync(
            RecurringBooking recurringBooking,
            List<RecurringBookingDetailRequest> detailRequests
    ) {
        if (recurringBooking == null || recurringBooking.getRecurringBookingId() == null) {
            return;
        }
        try {
            int windowDays = resolveGenerationWindowDays(recurringBooking);
            LocalDate anchor = LocalDate.now();
            LocalDate windowStart = resolveWindowStart(recurringBooking.getStartDate(), anchor);
            LocalDate windowEnd = resolveWindowEnd(windowStart, recurringBooking.getEndDate(), windowDays);

            Set<LocalDateTime> existingTimes = getExistingBookingTimes(
                    recurringBooking.getRecurringBookingId(),
                    windowStart,
                    windowEnd
            );

            List<LocalDateTime> planned = collectPlannedBookingTimes(
                    recurringBooking,
                    windowStart,
                    windowEnd,
                    existingTimes
            ).stream()
                    .filter(dt -> dt.isAfter(LocalDateTime.now()))
                    .sorted()
                    .toList();

            if (planned.isEmpty()) {
                return;
            }

            List<AssignmentRequest> recurringAssignments = buildRecurringAssignments(
                    recurringBooking,
                    planned,
                    detailRequests
            );

            generateBookingsAsync(
                    recurringBooking.getRecurringBookingId(),
                    planned,
                    recurringAssignments
            );
        } catch (Exception e) {
            log.error("Initial window generation failed for recurring booking {}: {}",
                    recurringBooking.getRecurringBookingId(), e.getMessage());
        }
    }

    private boolean shouldGenerateBookingForDate(
            RecurrenceType type,
            List<Integer> recurrenceDays,
            LocalDate date
    ) {
        if (type == RecurrenceType.WEEKLY) {
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
            boolean shouldGenerate = recurrenceDays.contains(dayOfWeek);
            log.debug("Checking WEEKLY date {}, dayOfWeek: {}, recurrenceDays: {}, shouldGenerate: {}", 
                    date, dayOfWeek, recurrenceDays, shouldGenerate);
            return shouldGenerate;
        } else if (type == RecurrenceType.MONTHLY) {
            int dayOfMonth = date.getDayOfMonth();
            boolean shouldGenerate = recurrenceDays.contains(dayOfMonth);
            log.debug("Checking MONTHLY date {}, dayOfMonth: {}, recurrenceDays: {}, shouldGenerate: {}", 
                    date, dayOfMonth, recurrenceDays, shouldGenerate);
            return shouldGenerate;
        }
        return false;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private String createBookingFromRecurring(RecurringBooking recurringBooking, LocalDateTime bookingDateTime, List<AssignmentRequest> recurringAssignments) {
        try {
            // Always fetch from database to ensure we have the latest data in a new transaction
            log.info("Fetching RecurringBooking with details for ID: {}", recurringBooking.getRecurringBookingId());
            RecurringBooking recurringBookingWithDetails = recurringBookingRepository
                    .findByIdWithDetails(recurringBooking.getRecurringBookingId())
                    .orElseThrow(() -> new RuntimeException("RecurringBooking not found: " + recurringBooking.getRecurringBookingId()));
            
            log.info("Fetched {} details for recurring booking", 
                    recurringBookingWithDetails.getRecurringBookingDetails().size());
            
            // Build BookingCreateRequest from RecurringBooking
            List<BookingDetailRequest> serviceDetails = recurringBookingWithDetails.getRecurringBookingDetails().stream()
                    .map(detail -> {
                        BigDecimal expectedPrice = detail.getPricePerUnit().multiply(BigDecimal.valueOf(detail.getQuantity()));
                        return new BookingDetailRequest(
                                detail.getService().getServiceId(),
                                detail.getQuantity(),
                                expectedPrice,
                                detail.getPricePerUnit(),
                                parseChoiceIds(detail.getSelectedChoiceIds())
                        );
                    })
                    .collect(Collectors.toList());

            BookingCreateRequest bookingRequest = new BookingCreateRequest(
                    recurringBookingWithDetails.getAddress().getAddressId(),
                    null, // newAddress
                    bookingDateTime,
                    recurringBookingWithDetails.getNote(),
                    null, // title - keep empty to allow auto-assign employees
                    null, // imageUrls
                    recurringBookingWithDetails.getPromotion() != null ? recurringBookingWithDetails.getPromotion().getPromoCode() : null,
                    serviceDetails,
                    (recurringAssignments != null && !recurringAssignments.isEmpty()) ? recurringAssignments : null,
                    1, // default payment method
                    null // additionalFeeIds
            );

            var summary = bookingService.createBooking(bookingRequest);

            // Link the booking to recurring booking
            if (summary != null && summary.getBookingId() != null) {
                Booking booking = bookingRepository.findById(summary.getBookingId()).orElse(null);
                if (booking != null) {
                    booking.setRecurringBooking(recurringBookingWithDetails);
                    bookingRepository.save(booking);
                }
                return summary.getBookingId();
            }

            return null;

        } catch (BookingValidationException e) {
            log.error("Booking validation failed for recurring booking: {}", e.getMessage());
            if (e.getErrors() != null && !e.getErrors().isEmpty()) {
                log.error("Validation errors: {}", e.getErrors());
            }
            return null;
        } catch (Exception e) {
            log.error("Error creating booking from recurring: {}", e.getMessage(), e);
            return null;
        }
    }

    private List<Integer> parseRecurrenceDays(String recurrenceDays) {
        if (recurrenceDays == null || recurrenceDays.isEmpty()) {
            return new ArrayList<>();
        }

        return java.util.Arrays.stream(recurrenceDays.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private List<Integer> parseChoiceIds(String choiceIds) {
        if (choiceIds == null || choiceIds.isEmpty()) {
            return new ArrayList<>();
        }

        return java.util.Arrays.stream(choiceIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private Set<LocalDateTime> getExistingBookingTimes(
            String recurringBookingId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (recurringBookingId == null) {
            return new HashSet<>();
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        List<LocalDateTime> times = bookingRepository.findBookingTimesByRecurringBooking(
                recurringBookingId, start, end);
        return new HashSet<>(times);
    }

    private List<LocalDateTime> collectPlannedBookingTimes(
            RecurringBooking recurringBooking,
            LocalDate startDate,
            LocalDate endDate,
            Set<LocalDateTime> existingTimes
    ) {
        List<LocalDateTime> times = new ArrayList<>();
        List<Integer> recurrenceDays = parseRecurrenceDays(recurringBooking.getRecurrenceDays());
        Set<LocalDateTime> cached = existingTimes != null ? existingTimes : new HashSet<>();

        LocalDate currentDate = startDate.isAfter(recurringBooking.getStartDate())
                ? startDate
                : recurringBooking.getStartDate();

        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            if (recurringBooking.getEndDate() != null && currentDate.isAfter(recurringBooking.getEndDate())) {
                break;
            }
            if (shouldGenerateBookingForDate(recurringBooking.getRecurrenceType(), recurrenceDays, currentDate)) {
                LocalDateTime bookingDateTime = LocalDateTime.of(currentDate, recurringBooking.getBookingTime());
                if (!cached.contains(bookingDateTime)) {
                    times.add(bookingDateTime);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return times;
    }

    private List<AssignmentRequest> buildRecurringAssignments(
            RecurringBooking recurringBooking,
            List<LocalDateTime> bookingTimes,
            List<RecurringBookingDetailRequest> detailRequests
    ) {
        if (bookingTimes == null || bookingTimes.isEmpty() || detailRequests == null || detailRequests.isEmpty()) {
            log.warn("No planned booking times or details; skip pre-assign employees for recurring booking {}", recurringBooking.getRecurringBookingId());
            return List.of();
        }

        Address address = recurringBooking.getAddress();
        String ward = address != null ? address.getWard() : null;
        String city = address != null ? address.getCity() : null;
        String customerId = recurringBooking.getCustomer() != null ? recurringBooking.getCustomer().getCustomerId() : null;

        List<AssignmentRequest> assignments = new ArrayList<>();

        // Chỉ cần kiểm tra trên tập thời gian nhỏ (đã giới hạn từ caller) để giảm tải
        for (RecurringBookingDetailRequest detailRequest : detailRequests) {
            SuitableEmployeeRequest suitableRequest = new SuitableEmployeeRequest(
                    detailRequest.serviceId(),
                    null,
                    ward,
                    city,
                    customerId,
                    bookingTimes
            );

            ApiResponse<List<SuitableEmployeeResponse>> response = employeeScheduleService.findSuitableEmployees(suitableRequest);
            if (response == null || !response.success() || response.data() == null || response.data().isEmpty()) {
                log.warn("No suitable employee found for service {} across {} slots; auto-assign fallback will be used per booking", detailRequest.serviceId(), bookingTimes.size());
                continue;
            }

            String employeeId = response.data().get(0).employeeId();
            assignments.add(new AssignmentRequest(employeeId, detailRequest.serviceId()));
            log.info("Pre-assigned employee {} for service {} across {} recurring slots", employeeId, detailRequest.serviceId(), bookingTimes.size());
        }

        return assignments;
    }

    private int calculateExpectedBookingsForWindow(
            RecurringBookingCreateRequest request,
            List<Integer> normalizedRecurrenceDays,
            int windowDays
    ) {
        LocalDate anchorDate = LocalDate.now();
        LocalDate windowStart = resolveWindowStart(request.startDate(), anchorDate);
        LocalDate windowEnd = resolveWindowEnd(windowStart, request.endDate(), windowDays);
        return calculateExpectedBookingsForWindow(
                request.recurrenceType(),
                normalizedRecurrenceDays,
                windowStart,
                windowEnd
        );
    }

    private int calculateExpectedBookingsForWindow(
            RecurrenceType recurrenceType,
            List<Integer> recurrenceDays,
            LocalDate windowStart,
            LocalDate windowEnd
    ) {
        return countOccurrences(recurrenceType, recurrenceDays, windowStart, windowEnd);
    }

    private int countOccurrences(
            RecurrenceType recurrenceType,
            List<Integer> recurrenceDays,
            LocalDate start,
            LocalDate end
    ) {
        if (recurrenceType == null || recurrenceDays == null || recurrenceDays.isEmpty() || start == null || end == null) {
            return 0;
        }
        if (start.isAfter(end)) {
            return 0;
        }

        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (shouldGenerateBookingForDate(recurrenceType, recurrenceDays, current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    private LocalDate resolveWindowStart(LocalDate startDate, LocalDate anchorDate) {
        if (startDate == null) {
            return anchorDate;
        }
        return startDate.isAfter(anchorDate) ? startDate : anchorDate;
    }

    private LocalDate resolveWindowEnd(LocalDate windowStart, LocalDate endDate, int windowDays) {
        LocalDate windowEnd = windowStart.plusDays(windowDays);
        if (endDate != null && endDate.isBefore(windowEnd)) {
            return endDate;
        }
        return windowEnd;
    }

    private int resolveGenerationWindowDays(RecurringBooking recurringBooking) {
        return recurringBooking != null && recurringBooking.getAssignedEmployee() != null
                ? ASSIGNED_GENERATION_WINDOW_DAYS
                : DEFAULT_GENERATION_WINDOW_DAYS;
    }

    private double calculateProgress(long generated, int expected) {
        if (expected <= 0) {
            return 100.0;
        }
        double progress = (generated * 100.0) / expected;
        return Math.min(progress, 100.0);
    }

    private void populateInitialExpectedStats(
            RecurringBookingResponse response,
            RecurringBookingCreateRequest request,
            List<Integer> normalizedRecurrenceDays
    ) {
        // set baseline
        response.setTotalGeneratedBookings(0);
        response.setUpcomingBookings(0);

        int windowDays = resolveGenerationWindowDays(null);
        LocalDate anchorDate = LocalDate.now();
        LocalDate windowStart = resolveWindowStart(request.startDate(), anchorDate);
        LocalDate windowEnd = resolveWindowEnd(windowStart, request.endDate(), windowDays);
        List<Integer> recurrenceDays = (normalizedRecurrenceDays != null && !normalizedRecurrenceDays.isEmpty())
                ? normalizedRecurrenceDays
                : request.recurrenceDays();
        int expected = calculateExpectedBookingsForWindow(
                request.recurrenceType(),
                recurrenceDays,
                windowStart,
                windowEnd
        );

        response.setExpectedBookingsInWindow(expected);
        response.setGeneratedBookingsInWindow(0);
        response.setGenerationWindowDays(windowDays);
        response.setGenerationProgressPercent(0.0);
    }

    private void setInitialWindowStats(
            RecurringBookingResponse response,
            RecurringBooking recurringBooking,
            List<Integer> normalizedRecurrenceDays
    ) {
        // Base stats from DB at the time of response
        long totalGenerated = bookingRepository.countByRecurringBooking_RecurringBookingId(
                recurringBooking.getRecurringBookingId()
        );
        response.setTotalGeneratedBookings((int) totalGenerated);

        LocalDateTime now = LocalDateTime.now();
        long upcoming = bookingRepository.countUpcomingByRecurringAndStatuses(
                recurringBooking.getRecurringBookingId(),
                now,
                List.of(BookingStatus.PENDING, BookingStatus.AWAITING_EMPLOYEE)
        );
        response.setUpcomingBookings((int) upcoming);

        int windowDays = resolveGenerationWindowDays(recurringBooking);
        LocalDate anchorDate = LocalDate.now();
        LocalDate windowStart = resolveWindowStart(recurringBooking.getStartDate(), anchorDate);
        LocalDate windowEnd = resolveWindowEnd(windowStart, recurringBooking.getEndDate(), windowDays);
        List<Integer> recurrenceDays = (normalizedRecurrenceDays != null && !normalizedRecurrenceDays.isEmpty())
                ? normalizedRecurrenceDays
                : parseRecurrenceDays(recurringBooking.getRecurrenceDays());
        int expected = calculateExpectedBookingsForWindow(
                recurringBooking.getRecurrenceType(),
                recurrenceDays,
                windowStart,
                windowEnd
        );
        long generatedInWindow = bookingRepository.countGeneratedRecurringBookingsInWindow(
                recurringBooking.getRecurringBookingId(),
                windowStart.atStartOfDay(),
                windowEnd.plusDays(1).atStartOfDay()
        );

        response.setExpectedBookingsInWindow(expected);
        response.setGeneratedBookingsInWindow((int) generatedInWindow);
        response.setGenerationWindowDays(windowDays);
        response.setGenerationProgressPercent(calculateProgress(generatedInWindow, expected));
    }
    
    private void updateStatistics(RecurringBookingResponse response, RecurringBooking recurringBooking) {
        try {
            // Count all bookings generated from this recurring booking
            long totalGenerated = bookingRepository.countByRecurringBooking_RecurringBookingId(
                recurringBooking.getRecurringBookingId()
            );
            response.setTotalGeneratedBookings((int) totalGenerated);
            
            // Count upcoming bookings (future bookings that are PENDING or AWAITING_EMPLOYEE)
            LocalDateTime now = LocalDateTime.now();
            long upcoming = bookingRepository.countUpcomingByRecurringAndStatuses(
                    recurringBooking.getRecurringBookingId(),
                    now,
                    List.of(BookingStatus.PENDING, BookingStatus.AWAITING_EMPLOYEE)
            );
            response.setUpcomingBookings((int) upcoming);

            int windowDays = resolveGenerationWindowDays(recurringBooking);
            List<Integer> recurrenceDays = parseRecurrenceDays(recurringBooking.getRecurrenceDays());
            LocalDate anchorDate = LocalDate.now();
            LocalDate windowStart = resolveWindowStart(recurringBooking.getStartDate(), anchorDate);
            LocalDate windowEnd = resolveWindowEnd(windowStart, recurringBooking.getEndDate(), windowDays);
            int expected = calculateExpectedBookingsForWindow(
                    recurringBooking.getRecurrenceType(),
                    recurrenceDays,
                    windowStart,
                    windowEnd
            );

            long generatedInWindow = bookingRepository.countGeneratedRecurringBookingsInWindow(
                    recurringBooking.getRecurringBookingId(),
                    windowStart.atStartOfDay(),
                    windowEnd.plusDays(1).atStartOfDay()
            );

            response.setExpectedBookingsInWindow(expected);
            response.setGeneratedBookingsInWindow((int) generatedInWindow);
            response.setGenerationWindowDays(windowDays);
            response.setGenerationProgressPercent(calculateProgress(generatedInWindow, expected));
        } catch (Exception e) {
            log.warn("Error calculating statistics for recurring booking {}: {}", 
                recurringBooking.getRecurringBookingId(), e.getMessage());
            response.setTotalGeneratedBookings(0);
            response.setUpcomingBookings(0);
            response.setExpectedBookingsInWindow(0);
            response.setGeneratedBookingsInWindow(0);
            response.setGenerationWindowDays(DEFAULT_GENERATION_WINDOW_DAYS);
            response.setGenerationProgressPercent(0.0);
        }
    }
}
