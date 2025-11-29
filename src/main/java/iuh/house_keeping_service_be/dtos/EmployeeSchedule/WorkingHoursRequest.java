package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Request DTO for creating/updating employee working hours
 */
public record WorkingHoursRequest(
    String employeeId,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Boolean isWorkingDay,
    LocalTime breakStartTime,
    LocalTime breakEndTime
) {}
