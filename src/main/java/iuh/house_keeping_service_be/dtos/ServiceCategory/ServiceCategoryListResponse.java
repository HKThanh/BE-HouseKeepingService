package iuh.house_keeping_service_be.dtos.ServiceCategory;

import java.util.List;

public record ServiceCategoryListResponse(
    boolean success,
    String message,
    List<ServiceCategoryData> data
) {}
