package iuh.house_keeping_service_be.dtos.Booking.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record AvailableBookingResponse(
        String bookingId,
        String bookingCode,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime bookingTime,
        String address,
        List<AvailableBookingDetail> details
) {}