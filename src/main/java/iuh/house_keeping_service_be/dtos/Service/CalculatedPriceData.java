package iuh.house_keeping_service_be.dtos.Service;

import java.math.BigDecimal;

public record CalculatedPriceData(
    Integer serviceId,
    String serviceName,
    BigDecimal basePrice,
    BigDecimal totalAdjustment,
    BigDecimal finalPrice,
    Integer suggestedStaff,
    BigDecimal estimatedDurationHours,
    String formattedPrice,
    String formattedDuration
) {}