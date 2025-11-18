package iuh.house_keeping_service_be.services.RecurringBookingService.impl;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingDetailRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingCancelRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingCreateRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingDetailRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingCreationSummary;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingResponse;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.enums.RecurrenceType;
import iuh.house_keeping_service_be.enums.RecurringBookingStatus;
import iuh.house_keeping_service_be.exceptions.*;
import iuh.house_keeping_service_be.mappers.RecurringBookingMapper;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.RecurringBookingService.RecurringBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringBookingServiceImpl implements RecurringBookingService {

    private final RecurringBookingRepository recurringBookingRepository;
    private final RecurringBookingDetailRepository recurringBookingDetailRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ServiceRepository serviceRepository;
    private final PromotionRepository promotionRepository;
    private final BookingRepository bookingRepository;
    private final RecurringBookingMapper recurringBookingMapper;
    private final BookingService bookingService;

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

            // Validate recurrence days
            validateRecurrenceDays(request.recurrenceType(), request.recurrenceDays());

            // Validate dates
            validateDates(request.startDate(), request.endDate());

            // Validate services
            List<Service> services = validateServices(request.bookingDetails());

            // Create recurring booking entity
            RecurringBooking recurringBooking = createRecurringBookingEntity(request, customer, address);

            // Set promotion if applicable
            if (request.promoCode() != null && !request.promoCode().trim().isEmpty()) {
                promotionRepository.findByPromoCode(request.promoCode())
                        .ifPresent(recurringBooking::setPromotion);
            }

            // Save recurring booking
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

            // Generate initial bookings for the next 30 days
            // Each booking creation will be in a separate transaction to avoid rollback-only issues
            List<String> generatedBookingIds = generateInitialBookings(savedRecurringBooking, request);

            log.info("Recurring booking created successfully with ID: {}", savedRecurringBooking.getRecurringBookingId());

            // Build response
            RecurringBookingResponse response = recurringBookingMapper.toResponse(
                    recurringBookingRepository.findById(savedRecurringBooking.getRecurringBookingId())
                            .orElseThrow()
            );
            
            // Update statistics with actual generated bookings count
            response.setTotalGeneratedBookings(generatedBookingIds.size());
            response.setUpcomingBookings(generatedBookingIds.size());

            RecurringBookingCreationSummary summary = new RecurringBookingCreationSummary();
            summary.setSuccess(true);
            summary.setMessage("Đặt lịch định kỳ thành công");
            summary.setRecurringBooking(response);
            summary.setGeneratedBookingIds(generatedBookingIds);
            summary.setTotalBookingsToBeCreated(calculateTotalBookings(request));

            return summary;

        } catch (CustomerNotFoundException | AddressNotFoundException | ServiceNotFoundException e) {
            log.error("Validation error creating recurring booking: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating recurring booking: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo lịch định kỳ: " + e.getMessage(), e);
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

            // Delete all future bookings (bookings with status PENDING or AWAITING_EMPLOYEE)
            LocalDateTime now = LocalDateTime.now();
            List<Booking> futureBookings = bookingRepository
                    .findAll()
                    .stream()
                    .filter(b -> b.getRecurringBooking() != null 
                            && b.getRecurringBooking().getRecurringBookingId().equals(recurringBookingId)
                            && b.getBookingTime().isAfter(now)
                            && (b.getStatus() == BookingStatus.PENDING 
                                || b.getStatus() == BookingStatus.AWAITING_EMPLOYEE))
                    .collect(Collectors.toList());

            log.info("Deleting {} future bookings", futureBookings.size());
            bookingRepository.deleteAll(futureBookings);

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
                    generateBookingsForPeriod(recurringBooking, today, futureDate);
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

    private void validateRecurrenceDays(RecurrenceType type, List<Integer> days) {
        if (days == null || days.isEmpty()) {
            throw new RuntimeException("Ngày lặp lại không được để trống");
        }

        for (Integer day : days) {
            if (type == RecurrenceType.WEEKLY) {
                if (day < 1 || day > 7) {
                    throw new RuntimeException("Ngày trong tuần phải từ 1 (Thứ 2) đến 7 (Chủ nhật)");
                }
            } else if (type == RecurrenceType.MONTHLY) {
                if (day < 1 || day > 31) {
                    throw new RuntimeException("Ngày trong tháng phải từ 1 đến 31");
                }
            }
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            throw new RuntimeException("Ngày bắt đầu phải từ hôm nay trở đi");
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private List<Service> validateServices(List<RecurringBookingDetailRequest> detailRequests) {
        List<Service> services = new ArrayList<>();

        for (RecurringBookingDetailRequest detail : detailRequests) {
            Service service = serviceRepository.findById(detail.serviceId())
                    .orElseThrow(() -> ServiceNotFoundException.withId(detail.serviceId()));

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
            Address address
    ) {
        RecurringBooking recurringBooking = new RecurringBooking();

        recurringBooking.setCustomer(customer);
        recurringBooking.setAddress(address);
        recurringBooking.setRecurrenceType(request.recurrenceType());
        recurringBooking.setRecurrenceDays(
                request.recurrenceDays().stream()
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
                log.info("Auto-calculated pricePerUnit for service {}: {}", service.getServiceId(), pricePerUnit);
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

    private List<String> generateInitialBookings(
            RecurringBooking recurringBooking,
            RecurringBookingCreateRequest request
    ) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30); // Generate for next 30 days

        return generateBookingsForPeriod(recurringBooking, today, futureDate);
    }

    private List<String> generateBookingsForPeriod(
            RecurringBooking recurringBooking,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<String> generatedBookingIds = new ArrayList<>();
        List<Integer> recurrenceDays = parseRecurrenceDays(recurringBooking.getRecurrenceDays());
        
        log.info("Generating bookings from {} to {} for recurring booking {}, recurrence days: {}, type: {}", 
                startDate, endDate, recurringBooking.getRecurringBookingId(), 
                recurrenceDays, recurringBooking.getRecurrenceType());

        LocalDate currentDate = startDate.isAfter(recurringBooking.getStartDate()) 
                ? startDate 
                : recurringBooking.getStartDate();
        
        log.info("Starting from date: {}, recurring booking start date: {}", 
                currentDate, recurringBooking.getStartDate());

        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            if (recurringBooking.getEndDate() != null && currentDate.isAfter(recurringBooking.getEndDate())) {
                break;
            }

            if (shouldGenerateBookingForDate(recurringBooking.getRecurrenceType(), recurrenceDays, currentDate)) {
                LocalDateTime bookingDateTime = LocalDateTime.of(currentDate, recurringBooking.getBookingTime());

                // Check if booking already exists for this date
                if (!bookingExistsForDateTime(recurringBooking, bookingDateTime)) {
                    try {
                        log.info("Creating booking for recurring booking {} on date {}", 
                                recurringBooking.getRecurringBookingId(), bookingDateTime);
                        String bookingId = createBookingFromRecurring(recurringBooking, bookingDateTime);
                        if (bookingId != null) {
                            generatedBookingIds.add(bookingId);
                            log.info("Successfully created booking {} for date {}", bookingId, bookingDateTime);
                        } else {
                            log.warn("createBookingFromRecurring returned null for date {}", bookingDateTime);
                        }
                    } catch (Exception e) {
                        log.error("Error creating booking for date {}: {}", currentDate, e.getMessage(), e);
                        // Don't throw, continue with next dates
                    }
                } else {
                    log.debug("Booking already exists for date {}", bookingDateTime);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        log.info("Generated {} bookings for recurring booking {}", 
                generatedBookingIds.size(), recurringBooking.getRecurringBookingId());

        return generatedBookingIds;
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

    private boolean bookingExistsForDateTime(RecurringBooking recurringBooking, LocalDateTime bookingDateTime) {
        // Query database instead of relying on lazy-loaded collection
        return bookingRepository.existsByRecurringBooking_RecurringBookingIdAndBookingTime(
                recurringBooking.getRecurringBookingId(), 
                bookingDateTime
        );
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private String createBookingFromRecurring(RecurringBooking recurringBooking, LocalDateTime bookingDateTime) {
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
                    recurringBookingWithDetails.getTitle(),
                    null, // imageUrls
                    recurringBookingWithDetails.getPromotion() != null ? recurringBookingWithDetails.getPromotion().getPromoCode() : null,
                    serviceDetails,
                    null, // assignments (will be auto-assigned)
                    1 // default payment method
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

    private Integer calculateTotalBookings(RecurringBookingCreateRequest request) {
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate() != null ? request.endDate() : startDate.plusMonths(12); // Default 1 year

        int count = 0;
        LocalDate currentDate = startDate;

        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            if (shouldGenerateBookingForDate(request.recurrenceType(), request.recurrenceDays(), currentDate)) {
                count++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return count;
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
            long upcoming = bookingRepository.findAll().stream()
                .filter(b -> b.getRecurringBooking() != null
                    && b.getRecurringBooking().getRecurringBookingId().equals(recurringBooking.getRecurringBookingId())
                    && b.getBookingTime().isAfter(now)
                    && (b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.AWAITING_EMPLOYEE))
                .count();
            response.setUpcomingBookings((int) upcoming);
        } catch (Exception e) {
            log.warn("Error calculating statistics for recurring booking {}: {}", 
                recurringBooking.getRecurringBookingId(), e.getMessage());
            response.setTotalGeneratedBookings(0);
            response.setUpcomingBookings(0);
        }
    }
}
