package iuh.house_keeping_service_be.dtos.Booking.internal;

import java.math.BigDecimal;

public record PricingRuleApplication(
        Integer ruleId,
        String ruleName,
        String conditionLogic,
        BigDecimal priceAdjustment,
        Integer staffAdjustment,
        Boolean isApplied
) {}