package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDate;

/**
 * Request DTO for getting available slots
 */
public record AvailableSlotsRequest(
    LocalDate date,              // Ngày cần tìm slot
    String ward,                 // Phường/xã
    String city,                 // Thành phố
    Integer serviceId,           // ID dịch vụ (để tính thời lượng)
    Integer durationMinutes,     // Hoặc thời lượng trực tiếp (nếu không có serviceId)
    Integer slotIntervalMinutes  // Khoảng cách giữa các slot (mặc định 30 phút)
) {
    public AvailableSlotsRequest {
        // Default slot interval to 30 minutes if not specified
        if (slotIntervalMinutes == null) {
            slotIntervalMinutes = 30;
        }
    }
}
