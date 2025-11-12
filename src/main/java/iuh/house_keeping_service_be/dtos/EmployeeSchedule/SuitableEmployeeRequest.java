package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDateTime;
import java.util.List;

public record SuitableEmployeeRequest(
    Integer serviceId,
    LocalDateTime bookingTime,
    String ward,
    String city,
    String customerId,  // Optional: để lọc và ưu tiên nhân viên đã từng phục vụ customer này
    List<LocalDateTime> bookingTimes  // Optional: danh sách thời gian để kiểm tra tất cả các slot
) {}