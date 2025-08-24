package iuh.house_keeping_service_be.dtos.Service;

import java.math.BigDecimal;

public record ServiceDetailResponse(
    boolean success,
    String message,
    ServiceDetailData data
) {}

