package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.time.LocalDateTime;

public record TimeSlot(
    LocalDateTime startTime,
    LocalDateTime endTime,
    String type, // "BUSY", "AVAILABLE", "UNAVAILABLE", "ASSIGNMENT"
    String reason,
    String bookingCode,
    String serviceName,
    String customerName,
    String address,
    String status,
    Integer durationHours
) {}
