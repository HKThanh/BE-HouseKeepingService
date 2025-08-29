package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDateTime;

public record SuitableEmployeeRequest(
    Integer serviceId,
    LocalDateTime bookingTime,
    String district,
    String city
) {}