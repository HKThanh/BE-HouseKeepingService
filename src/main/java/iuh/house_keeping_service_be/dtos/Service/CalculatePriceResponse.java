package iuh.house_keeping_service_be.dtos.Service;

import java.math.BigDecimal;

public record CalculatePriceResponse(
    Boolean success,
    String message,
    CalculatedPriceData data
) {}