package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record BookingDetailRequest(
        @NotNull(message = "Service ID is required")
        @Min(value = 1, message = "Service ID must be positive")
        Integer serviceId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 10, message = "Quantity cannot exceed 10")
        Integer quantity,

        List<@Min(value = 1, message = "Choice ID must be positive") Integer> selectedChoiceIds
) {}