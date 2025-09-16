package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record BookingCreateRequest(
        @NotBlank(message = "Address ID is required")
        String addressId,

        @NotNull(message = "Booking time is required")
        @Future(message = "Booking time must be in the future")
        LocalDateTime bookingTime,

        @Size(max = 1000, message = "Note cannot exceed 1000 characters")
        String note,

        @Size(max = 20, message = "Promo code cannot exceed 20 characters")
        String promoCode,

        @NotEmpty(message = "Booking details cannot be empty")
        @Valid
        List<BookingDetailRequest> bookingDetails,

        @Valid
        List<AssignmentRequest> assignments,

        int paymentMethodId
) {}