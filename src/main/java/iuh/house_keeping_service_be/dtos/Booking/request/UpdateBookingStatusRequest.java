package iuh.house_keeping_service_be.dtos.Booking.request;

import iuh.house_keeping_service_be.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingStatusRequest {
    
    @NotNull(message = "Status không được để trống")
    private BookingStatus status;
    
    private String adminComment;
}
