package iuh.house_keeping_service_be.dtos.RecurringBooking.request;

import iuh.house_keeping_service_be.dtos.Booking.request.NewAddressRequest;
import iuh.house_keeping_service_be.enums.RecurrenceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request DTO for creating a recurring booking
 */
public record RecurringBookingCreateRequest(
        String addressId,

        @Valid
        NewAddressRequest newAddress,

        @NotNull(message = "Recurrence type is required")
        RecurrenceType recurrenceType,

        @NotEmpty(message = "Recurrence days cannot be empty")
        List<@NotNull(message = "Day value cannot be null") @Min(value = 1, message = "Day must be at least 1") @Max(value = 31, message = "Day cannot exceed 31") Integer> recurrenceDays,

        @NotNull(message = "Booking time is required")
        LocalTime bookingTime,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDate startDate,

        LocalDate endDate, // Optional

        @Size(max = 1000, message = "Note cannot exceed 1000 characters")
        String note,

        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        @Size(max = 20, message = "Promo code cannot exceed 20 characters")
        String promoCode,

        @NotEmpty(message = "Booking details cannot be empty")
        @Valid
        List<RecurringBookingDetailRequest> bookingDetails
) {
}
