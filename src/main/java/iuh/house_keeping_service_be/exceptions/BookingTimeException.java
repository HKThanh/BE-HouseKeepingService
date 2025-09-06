package iuh.house_keeping_service_be.exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookingTimeException extends RuntimeException {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    
    public BookingTimeException(String message, LocalDateTime startTime, LocalDateTime endTime) {
        super(message);
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    // Static factory methods
    public static BookingTimeException invalidRange(LocalDateTime start, LocalDateTime end) {
        String message = String.format("Invalid booking time range: start time %s is after end time %s",
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return new BookingTimeException(message, start, end);
    }
    
    public static BookingTimeException outsideBusinessHours(LocalDateTime start, LocalDateTime end) {
        String message = String.format("Booking time %s to %s is outside business hours",
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return new BookingTimeException(message, start, end);
    }
    
    public static BookingTimeException inThePast(LocalDateTime requestedStart) {
        String message = String.format("Booking start time %s is in the past",
                requestedStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return new BookingTimeException(message, requestedStart, null);
    }
}