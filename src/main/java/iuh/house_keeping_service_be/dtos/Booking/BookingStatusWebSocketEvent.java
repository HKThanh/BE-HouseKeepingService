package iuh.house_keeping_service_be.dtos.Booking;

import iuh.house_keeping_service_be.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusWebSocketEvent {
    private String bookingId;
    private String bookingCode;
    private BookingStatus status;
    private String trigger;
    private TriggeredBy triggeredBy;
    private String note;
    private LocalDateTime at;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggeredBy {
        private String accountId;
        private String role;
        private String name;
    }
}
