package iuh.house_keeping_service_be.dtos.Booking.internal;

import java.time.LocalDateTime;
import java.util.List;

public record EmployeeAvailabilityResult(
        String employeeId,
        String fullName,
        Integer serviceId,
        Boolean isAvailable,
        List<ConflictInfo> conflicts,
        String reasonUnavailable
) {}