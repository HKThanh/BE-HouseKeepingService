package iuh.house_keeping_service_be.dtos.Service;

import java.math.BigDecimal;
import java.util.List;

public record ServiceOptionsData(
    Integer serviceId,
    String serviceName,
    String description,
    BigDecimal basePrice,
    String unit,
    BigDecimal estimatedDurationHours,
    String formattedPrice,
    String formattedDuration,
    List<ServiceOptionData> options
) {}