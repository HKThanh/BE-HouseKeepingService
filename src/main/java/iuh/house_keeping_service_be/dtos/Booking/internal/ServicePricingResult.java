package iuh.house_keeping_service_be.dtos.Booking.internal;

import java.math.BigDecimal;
import java.util.List;

public record ServicePricingResult(
        Integer serviceId,
        String serviceName,
        BigDecimal basePrice,
        Integer quantity,
        List<ChoicePricingInfo> selectedChoices,
        List<PricingRuleApplication> appliedRules,
        BigDecimal calculatedSubTotal,
        Integer requiredStaffCount
) {}