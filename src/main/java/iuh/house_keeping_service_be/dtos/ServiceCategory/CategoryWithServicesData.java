package iuh.house_keeping_service_be.dtos.ServiceCategory;

import iuh.house_keeping_service_be.dtos.Service.ServiceData;

import java.util.List;

public record CategoryWithServicesData(
    Integer categoryId,
    String categoryName,
    String description,
    List<ServiceData> services
) {}


