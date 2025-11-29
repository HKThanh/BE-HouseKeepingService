package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Response DTO for employee working hours
 */
public record WorkingHoursResponse(
    String workingHoursId,
    DayOfWeek dayOfWeek,
    String dayOfWeekDisplay,  // Tên ngày tiếng Việt
    LocalTime startTime,
    LocalTime endTime,
    Boolean isWorkingDay,
    LocalTime breakStartTime,
    LocalTime breakEndTime
) {
    public static String getDayOfWeekDisplay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }
}
