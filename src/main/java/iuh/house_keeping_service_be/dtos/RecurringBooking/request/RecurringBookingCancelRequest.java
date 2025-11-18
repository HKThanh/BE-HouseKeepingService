package iuh.house_keeping_service_be.dtos.RecurringBooking.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for cancelling a recurring booking
 */
public record RecurringBookingCancelRequest(
        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 1000, message = "Cancellation reason cannot exceed 1000 characters")
        String reason
) {
}
