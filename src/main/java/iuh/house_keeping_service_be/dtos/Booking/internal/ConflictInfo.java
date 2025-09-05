package iuh.house_keeping_service_be.dtos.Booking.internal;

import java.time.LocalDateTime;

public record ConflictInfo(
        String conflictType, // "BOOKING" or "UNAVAILABLE"
        String conflictId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description
) {}