package iuh.house_keeping_service_be.dtos.Booking.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultipleBookingCreationSummary {
    private int totalBookingsCreated;
    private int successfulBookings;
    private int failedBookings;
    private BigDecimal totalAmount;
    private String formattedTotalAmount;
    private List<BookingCreationSummary> bookings;
    private List<BookingCreationError> errors;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingCreationError {
        private int index;
        private String bookingTime;
        private String errorMessage;
        private List<String> details;
    }
}
