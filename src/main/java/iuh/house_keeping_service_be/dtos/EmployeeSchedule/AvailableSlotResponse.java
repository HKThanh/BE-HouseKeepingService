package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for an available time slot
 */
public record AvailableSlotResponse(
    LocalDateTime startTime,
    LocalDateTime endTime,
    Integer durationMinutes,
    Integer availableEmployeeCount,
    List<AvailableEmployeeInfo> availableEmployees
) {
    public record AvailableEmployeeInfo(
        String employeeId,
        String fullName,
        String avatar,
        String rating,
        List<String> skills
    ) {}
}
