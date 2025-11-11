package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDateTime;

public record SuitableEmployeeRequest(
    Integer serviceId,
    LocalDateTime bookingTime,
    String ward,
    String city,
    String customerId  // Optional: để lọc và ưu tiên nhân viên đã từng phục vụ customer này
) {}