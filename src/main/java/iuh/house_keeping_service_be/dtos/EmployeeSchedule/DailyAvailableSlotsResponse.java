package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for available slots grouped by date
 */
public record DailyAvailableSlotsResponse(
    LocalDate date,
    String dayOfWeek,           // Tên ngày trong tuần
    Integer totalSlots,
    Integer totalAvailableEmployees,
    List<AvailableSlotResponse> slots
) {}
