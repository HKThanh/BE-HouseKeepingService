package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.util.List;

public record EmployeeScheduleResponse(
    String employeeId,
    String fullName,
    String avatar,
    List<String> skills,
    String rating,
    String employeeStatus,
    List<WorkingZone> workingZones,
    List<TimeSlot> timeSlots
) {}
