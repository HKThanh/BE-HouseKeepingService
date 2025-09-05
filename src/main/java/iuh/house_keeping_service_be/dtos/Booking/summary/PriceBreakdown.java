package iuh.house_keeping_service_be.dtos.Booking.summary;

import java.math.BigDecimal;

public record PriceBreakdown(
        String description,
        BigDecimal amount,
        String type // "SERVICE", "CHOICE", "RULE", "DISCOUNT"
) {}