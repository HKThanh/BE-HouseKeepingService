package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDateTime;

public record UnavailabilityRequest(
    String employeeId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String reason
) {}
