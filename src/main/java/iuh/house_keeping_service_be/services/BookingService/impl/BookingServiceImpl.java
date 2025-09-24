package iuh.house_keeping_service_be.services.BookingService.impl;

import iuh.house_keeping_service_be.dtos.Booking.internal.*;
import iuh.house_keeping_service_be.dtos.Booking.request.*;
import iuh.house_keeping_service_be.dtos.Booking.response.*;
import iuh.house_keeping_service_be.dtos.Booking.summary.*;
import iuh.house_keeping_service_be.dtos.Service.*;
import iuh.house_keeping_service_be.enums.*;
import iuh.house_keeping_service_be.exceptions.*;
import iuh.house_keeping_service_be.mappers.BookingMapper;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import iuh.house_keeping_service_be.services.ServiceService.ServiceService;
import iuh.house_keeping_service_be.utils.BookingDTOFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    
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
    
    // Other Services
    private final ServiceService serviceService;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
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

            // Step 2: Re-validate employee availability (double-check for conflicts) if assignments provided
            if (hasAssignments) {
                validateEmployeeAvailabilityFinal(request);
            }
            
            // Step 3: Create booking entity
            Booking booking = createBookingEntity(request, validation);

            if (!hasAssignments) {
                booking.setStatus(BookingStatus.AWAITING_EMPLOYEE);
            }
            
            // Step 4: Create booking details
            List<BookingDetail> bookingDetails = createBookingDetails(booking, request, validation);

            // Step 5: Create assignments if provided
            List<Assignment> assignments = hasAssignments
                    ? createAssignments(bookingDetails, request)
                    : Collections.emptyList();

            // Step 6: Create payment record
            Payment payment = createPaymentRecord(booking, request.paymentMethodId());
            
            // Step 7: Save all entities
            Booking savedBooking = bookingRepository.save(booking);
            List<BookingDetail> savedDetails = bookingDetailRepository.saveAll(bookingDetails);
            List<Assignment> savedAssignments = hasAssignments
                    ? assignmentRepository.saveAll(assignments)
                    : Collections.emptyList();
            Payment savedPayment = paymentRepository.save(payment);
            
            log.info("Booking created successfully with ID: {}", savedBooking.getBookingId());
            
            // Step 8: Return creation summary
            return createBookingCreationSummary(savedBooking, savedDetails, savedAssignments, savedPayment);
            
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

            BookingValidationResult successResult = BookingValidationResult.success(finalAmount, serviceValidations, customer, addressContext.address(), addressContext.isNewAddress());
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
        address.setDistrict(newAddress.district());
        address.setCity(newAddress.city());
        address.setLatitude(newAddress.latitude() != null ? BigDecimal.valueOf(newAddress.latitude()) : null);
        address.setLongitude(newAddress.longitude() != null ? BigDecimal.valueOf(newAddress.longitude()) : null);
        address.setIsDefault(Boolean.FALSE);

        return new CustomerAddressContext(customer, address, true);
    }

    private void validateBookingTime(LocalDateTime bookingTime, List<String> errors) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if booking time is in the future
        if (bookingTime.isBefore(now.plusHours(2))) {
            errors.add("Booking time must be at least 2 hours from now");
        }
        
        // Check if booking time is within business hours (8 AM - 8 PM)
        int hour = bookingTime.getHour();
        if (hour < 8 || hour > 20) {
            errors.add("Booking time must be between 8:00 AM and 8:00 PM");
        }
        
        // Check if booking is not too far in the future (within 30 days)
        if (bookingTime.isAfter(now.plusDays(30))) {
            errors.add("Booking time cannot be more than 30 days from now");
        }
    }

    private List<ServiceValidationInfo> validateServices(List<BookingDetailRequest> detailRequests, List<String> errors) {
        return detailRequests.stream()
            .map(detail -> validateSingleService(detail, errors))
            .toList();
    }

    private ServiceValidationInfo validateSingleService(BookingDetailRequest detail, List<String> errors) {
        // Find service - sử dụng Optional properly
        Optional<iuh.house_keeping_service_be.models.Service> serviceOpt = 
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
        BigDecimal calculatedPrice = calculateServicePrice(detail);
        
        // Compare with expected price (tolerance of 1000 VND)
        BigDecimal priceDifference = calculatedPrice.subtract(detail.expectedPrice()).abs();
        boolean priceMatches = priceDifference.compareTo(new BigDecimal("1000")) <= 0;
        
        if (!priceMatches) {
            errors.add(String.format("Price mismatch for service %d. Expected: %s, Calculated: %s", 
                detail.serviceId(), 
                BookingDTOFormatter.formatPrice(detail.expectedPrice()),
                BookingDTOFormatter.formatPrice(calculatedPrice)));
        }
        
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
            .recommendedStaff(service.getRecommendedStaff() != null ? service.getRecommendedStaff() : 1)
            .build();
    }

    // Fixed calculateServicePrice method to use existing ServiceService
    private BigDecimal calculateServicePrice(BookingDetailRequest detail) {
        try {
            // Create CalculatePriceRequest using existing ServiceService method
            CalculatePriceRequest priceRequest = new CalculatePriceRequest(
                detail.serviceId(),
                detail.selectedChoiceIds() != null ? detail.selectedChoiceIds() : List.of(),
                detail.quantity()
            );
            
            // Call existing calculatePrice method
            CalculatePriceResponse priceResponse = serviceService.calculatePrice(priceRequest);
            
            if (priceResponse.success() && priceResponse.data() != null) {
                return priceResponse.data().finalPrice();
            } else {
                log.warn("Price calculation failed for service {}: {}", detail.serviceId(), priceResponse.message());
                return detail.expectedPrice(); // Fallback to expected price
            }
            
        } catch (Exception e) {
            log.error("Error calculating price for service {}: {}", detail.serviceId(), e.getMessage());
            return detail.expectedPrice(); // Fallback to expected price
        }
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

    // Fixed validateEmployeeAssignments - sử dụng đúng constructor ConflictInfo
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

    // Fixed checkEmployeeAvailability - sử dụng đúng constructor ConflictInfo
    private void checkEmployeeAvailability(Employee employee, Integer serviceId, 
                                         LocalDateTime bookingTime, 
                                         List<ConflictInfo> conflicts) {
        // Get service duration for time range calculation
        Optional<iuh.house_keeping_service_be.models.Service> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isEmpty()) return;
        
        var service = serviceOpt.get();
        LocalDateTime endTime = bookingTime.plusHours(service.getEstimatedDurationHours().longValue());
        
        // Check for conflicting assignments
        List<Assignment> conflictingAssignments = assignmentRepository.findConflictingAssignments(
            employee.getEmployeeId(), bookingTime, endTime);
        
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
            iuh.house_keeping_service_be.models.Service service = serviceRepository.findById(detailRequest.serviceId())
                .orElseThrow(() -> ServiceNotFoundException.withId(detailRequest.serviceId()));
            detail.setService(service);
            
            detail.setQuantity(detailRequest.quantity());
            detail.setPricePerUnit(detailRequest.expectedPricePerUnit());
            detail.setSubTotal(serviceValidation.getCalculatedPrice());
            
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
                                                              Payment payment) {
        // Map to DTOs
        CustomerAddressInfo addressInfo = bookingMapper.toCustomerAddressInfo(booking.getAddress());
        List<BookingDetailInfo> detailInfos = details.stream()
            .map(bookingMapper::toBookingDetailInfo)
            .toList();
        PaymentInfo paymentInfo = bookingMapper.toPaymentInfo(payment);
        PromotionInfo promotionInfo = booking.getPromotion() != null ? 
            bookingMapper.toPromotionInfo(booking.getPromotion()) : null;
        
        // Create summary
        BookingCreationSummary summary = BookingCreationSummary.builder()
            .bookingId(booking.getBookingId())
            .bookingCode(booking.getBookingCode())
            .status(booking.getStatus().toString())
            .totalAmount(booking.getTotalAmount())
            .formattedTotalAmount(BookingDTOFormatter.formatPrice(booking.getTotalAmount()))
            .bookingTime(booking.getBookingTime())
            .customerInfo(addressInfo)
            .serviceDetails(detailInfos)
            .paymentInfo(paymentInfo)
            .promotionApplied(promotionInfo)
            .assignedEmployees(assignments.stream()
                .map(a -> bookingMapper.toEmployeeInfo(a.getEmployee()))
                .toList())
            .createdAt(booking.getCreatedAt())
            .build();

        try {
            summary.calculateSummaryFields();
        } catch (Exception e) {
            log.warn("Could not calculate summary fields: {}", e.getMessage());
        }
        
        return summary;
    }

    private record CustomerAddressContext(Customer customer, Address address, boolean isNewAddress) {
    }

    private record ValidationOutcome(BookingValidationResult result, CustomerAddressContext addressContext) {
    }
}