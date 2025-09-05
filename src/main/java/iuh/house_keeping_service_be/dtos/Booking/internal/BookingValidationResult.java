package iuh.house_keeping_service_be.dtos.Booking.internal;

import java.math.BigDecimal;
import java.util.List;

public record BookingValidationResult(
        Boolean isValid,
        BigDecimal calculatedTotalAmount,
        BigDecimal discountAmount,
        List<ServicePricingResult> servicePricingResults,
        List<EmployeeAvailabilityResult> employeeAvailabilityResults,
        List<String> validationErrors
) {}