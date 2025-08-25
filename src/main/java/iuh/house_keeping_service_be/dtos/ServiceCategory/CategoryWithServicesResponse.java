package iuh.house_keeping_service_be.dtos.ServiceCategory;

public record CategoryWithServicesResponse(
    boolean success,
    String message,
    CategoryWithServicesData data
) {}

