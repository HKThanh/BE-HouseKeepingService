package iuh.house_keeping_service_be.dtos.ServiceCategory;

public record ServiceCategoryData(
    Integer categoryId,
    String categoryName,
    String description,
    String iconUrl,
    Boolean isActive,
    Integer serviceCount
) {}
