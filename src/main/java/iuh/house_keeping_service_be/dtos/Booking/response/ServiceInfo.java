package iuh.house_keeping_service_be.dtos.Booking.response;

import java.math.BigDecimal;

public record ServiceInfo(
        Integer serviceId,
        String name,
        String description,
        BigDecimal basePrice,
        String unit,
        Double estimatedDurationHours,
        String categoryName,
        Boolean isActive
) {}