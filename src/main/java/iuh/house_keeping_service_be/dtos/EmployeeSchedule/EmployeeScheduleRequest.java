package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDateTime;
import java.util.List;

public record EmployeeScheduleRequest(
    String employeeId,
    LocalDateTime startDate,
    LocalDateTime endDate,
    String district,
    String city
) {}

