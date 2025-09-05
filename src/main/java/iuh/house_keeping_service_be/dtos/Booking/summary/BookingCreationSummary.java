package iuh.house_keeping_service_be.dtos.Booking.summary;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record BookingCreationSummary(
        String bookingId,
        String bookingCode,
        Boolean isCreated,
        String message,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        BookingPriceCalculation pricing
) {}