package iuh.house_keeping_service_be.dtos.RecurringBooking.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for recurring booking detail (service item)
 * Unlike regular BookingDetailRequest, expectedPrice and expectedPricePerUnit are optional
 * and will be calculated automatically if not provided
 */
public record RecurringBookingDetailRequest(
        @NotNull(message = "Service ID is required")
        @Min(value = 1, message = "Service ID must be positive")
        Integer serviceId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 10, message = "Quantity cannot exceed 10")
        Integer quantity,

        // Optional - will be calculated if not provided
        @DecimalMin(value = "0.0", inclusive = false, message = "Expected price must be positive")
        BigDecimal expectedPrice,

        // Optional - will be calculated if not provided
        @DecimalMin(value = "0.0", inclusive = false, message = "Expected price per unit must be positive")
        BigDecimal expectedPricePerUnit,

        List<@Min(value = 1, message = "Choice ID must be positive") Integer> selectedChoiceIds
) {
    public RecurringBookingDetailRequest {
        if (selectedChoiceIds == null) {
            selectedChoiceIds = List.of();
        }
        
        // Validate that total price matches (price per unit * quantity) if both are provided
        if (expectedPrice != null && expectedPricePerUnit != null && quantity != null) {
            BigDecimal calculatedTotal = expectedPricePerUnit.multiply(BigDecimal.valueOf(quantity));
            if (expectedPrice.compareTo(calculatedTotal) != 0) {
                throw new IllegalArgumentException("Expected price must equal price per unit multiplied by quantity");
            }
        }
    }
}
