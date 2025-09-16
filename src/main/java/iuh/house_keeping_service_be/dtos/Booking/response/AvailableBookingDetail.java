package iuh.house_keeping_service_be.dtos.Booking.response;

import java.math.BigDecimal;

public record AvailableBookingDetail(
        String bookingDetailId,
        String serviceName,
        Integer quantity,
        BigDecimal pricePerUnit
) {}