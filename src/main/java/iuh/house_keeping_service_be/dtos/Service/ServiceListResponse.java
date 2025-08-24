package iuh.house_keeping_service_be.dtos.Service;

import iuh.house_keeping_service_be.dtos.Service.ServiceData;

import java.util.List;

public record ServiceListResponse(
    boolean success,
    String message,
    List<ServiceData> data
) {}

