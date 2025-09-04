package iuh.house_keeping_service_be.dtos.Service;

public record ServiceOptionsResponse(
    Boolean success,
    String message,
    ServiceOptionsData data
) {}