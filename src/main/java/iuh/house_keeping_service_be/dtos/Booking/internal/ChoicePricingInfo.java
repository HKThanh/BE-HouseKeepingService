package iuh.house_keeping_service_be.dtos.Booking.internal;

import java.math.BigDecimal;

public record ChoicePricingInfo(
        Integer choiceId,
        String choiceLabel,
        BigDecimal extraFee,
        Integer optionId,
        String optionLabel
) {}