package iuh.house_keeping_service_be.dtos.Service;

import java.math.BigDecimal;

public record ServiceData(
    Integer serviceId,
    String name,
    String description,
    BigDecimal basePrice,
    String unit,
    BigDecimal estimatedDurationHours,
    Integer recommendedStaff,
    String iconUrl,
    Boolean isActive
) {}
