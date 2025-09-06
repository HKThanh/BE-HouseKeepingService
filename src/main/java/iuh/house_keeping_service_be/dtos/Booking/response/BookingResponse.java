package iuh.house_keeping_service_be.dtos.Booking.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private boolean success;
    private String message;
    private BookingData data;
    
    public static BookingResponse success(BookingData data) {
        return new BookingResponse(true, "Đặt lịch thành công", data);
    }
    
    public static BookingResponse success(String message, BookingData data) {
        return new BookingResponse(true, message, data);
    }
    
    public static BookingResponse error(String message) {
        return new BookingResponse(false, message, null);
    }
}