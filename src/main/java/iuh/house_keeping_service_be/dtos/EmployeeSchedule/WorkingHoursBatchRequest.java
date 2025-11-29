package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Batch request for setting all working hours for an employee
 */
public record WorkingHoursBatchRequest(
    String employeeId,
    List<DayWorkingHours> weeklySchedule
) {
    public record DayWorkingHours(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isWorkingDay,
        LocalTime breakStartTime,
        LocalTime breakEndTime
    ) {}
}
