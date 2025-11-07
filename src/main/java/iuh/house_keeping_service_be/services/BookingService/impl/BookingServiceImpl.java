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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
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
    private final PricingRuleRepository pricingRuleRepository;
    private final RuleConditionRepository ruleConditionRepository;

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

            // Set isVerified based on assignments
            // If no assignments (empty list), set isVerified = false (becomes a post needing admin approval)
            // If has assignments, set isVerified = true (normal booking)
            booking.setIsVerified(hasAssignments);

            // Set title and imageUrls from request if provided
            if (request.title() != null && !request.title().trim().isEmpty()) {
                booking.setTitle(request.title());
            }
            if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
                booking.setImageUrls(request.imageUrls());
            }

            log.info("Booking isVerified={}, hasAssignments={}, title={}, imageUrls={}",
                    booking.getIsVerified(), hasAssignments, booking.getTitle(), booking.getImageUrls());

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
                    assignment.setStatus(AssignmentStatus.PENDING);  // Changed from ASSIGNED to PENDING

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

        // Create summary
        BookingCreationSummary summary = BookingCreationSummary.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus().toString())
                .totalAmount(booking.getTotalAmount())
                .formattedTotalAmount(BookingDTOFormatter.formatPrice(booking.getTotalAmount()))
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

            return new BookingHistoryResponse(
                    booking.getBookingId(),
                    booking.getBookingCode(),
                    booking.getCustomer().getCustomerId(),
                    booking.getCustomer().getFullName(),
                    addressInfo,
                    booking.getBookingTime().toString(),
                    booking.getNote(),
                    BookingDTOFormatter.formatPrice(booking.getTotalAmount()),
                    booking.getStatus().toString(),
                    promotionInfo,
                    paymentInfo,
                    booking.getTitle(),
                    booking.getImageUrls(),
                    booking.getIsVerified(),
                    assignedEmployees,
                    services
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

            return new BookingHistoryResponse(
                    booking.getBookingId(),
                    booking.getBookingCode(),
                    booking.getCustomer().getCustomerId(),
                    booking.getCustomer().getFullName(),
                    addressInfo,
                    booking.getBookingTime().toString(),
                    booking.getNote(),
                    BookingDTOFormatter.formatPrice(booking.getTotalAmount()),
                    booking.getStatus().toString(),
                    promotionInfo,
                    paymentInfo,
                    booking.getTitle(),
                    booking.getImageUrls(),
                    booking.getIsVerified(),
                    assignedEmployees,
                    services
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

            // TODO: Send notification to customer about approval

        } else {
            // Reject the booking post
            log.info("Booking {} has been rejected by admin. Reason: {}",
                    bookingId, request.rejectionReason());

            // Save rejection reason as admin comment
            if (request.rejectionReason() != null && !request.rejectionReason().trim().isEmpty()) {
                booking.setAdminComment(request.rejectionReason());
            }

            booking.setIsVerified(true);

            // TODO: Send notification to customer about rejection with reason
            // For now, we'll just cancel the booking
            booking.setStatus(BookingStatus.CANCELLED);
        }

        Booking savedBooking = bookingRepository.save(booking);

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
        List<BookingDetail> bookingDetails = booking.getBookingDetails();
        if (bookingDetails != null && !bookingDetails.isEmpty()) {
            for (BookingDetail detail : bookingDetails) {
                List<Assignment> assignments = assignmentRepository.findByBookingDetailId(detail.getId());
                if (assignments != null && !assignments.isEmpty()) {
                    for (Assignment assignment : assignments) {
                        if (assignment.getStatus() != AssignmentStatus.CANCELLED &&
                                assignment.getStatus() != AssignmentStatus.COMPLETED) {
                            assignment.setStatus(AssignmentStatus.CANCELLED);
                            assignmentRepository.save(assignment);
                            log.info("Cancelled assignment {} for employee {}",
                                    assignment.getAssignmentId(),
                                    assignment.getEmployee().getEmployeeId());
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

        log.info("Booking {} cancelled successfully by customer {}", bookingId, customerId);

        // TODO: Send notification to assigned employees about cancellation
        // TODO: Send notification to customer about cancellation confirmation
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
        log.info("Admin fetching all bookings sorted by booking time descending");

        Page<Booking> allBookings = bookingRepository.findAllBookingsOrderByBookingTimeDesc(pageable);

        Page<BookingResponse> response = allBookings.map(bookingMapper::toBookingResponse);

        log.info("Found {} bookings (page {} of {})",
                allBookings.getNumberOfElements(),
                allBookings.getNumber() + 1,
                allBookings.getTotalPages());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookingsSortedByBookingTime(LocalDateTime fromDate, Pageable pageable) {
        log.info("Admin fetching all bookings sorted by booking time descending with fromDate: {}", fromDate);

        Page<Booking> allBookings;
        if (fromDate != null) {
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

        return bookingMapper.toBookingResponse(savedBooking);
    }
}
