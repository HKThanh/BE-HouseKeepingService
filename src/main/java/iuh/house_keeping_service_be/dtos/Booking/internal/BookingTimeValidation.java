package iuh.house_keeping_service_be.dtos.Booking.internal;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingTimeValidation {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookingTime;
    
    private boolean isValidTime;
    private boolean isBusinessHours;
    private boolean isNotPast;
    private boolean isWithinBookingWindow;
    private String validationMessage;
    
    public static BookingTimeValidation valid(LocalDateTime bookingTime) {
        return new BookingTimeValidation(bookingTime, true, true, true, true, "Thời gian đặt lịch hợp lệ");
    }
    
    public static BookingTimeValidation invalid(LocalDateTime bookingTime, String message) {
        return new BookingTimeValidation(bookingTime, false, false, false, false, message);
    }
}