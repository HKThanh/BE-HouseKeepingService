package iuh.house_keeping_service_be.services.WebSocketNotificationService;

import iuh.house_keeping_service_be.dtos.Assignment.AssignmentProgressWebSocketEvent;
import iuh.house_keeping_service_be.dtos.Booking.BookingStatusWebSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingRealtimeEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishAssignmentProgress(AssignmentProgressWebSocketEvent event) {
        if (event == null || event.getBookingId() == null) {
            log.warn("Skip publishing assignment progress because event or bookingId is null");
            return;
        }

        String destination = String.format("/topic/bookings/%s/assignments", event.getBookingId());
        messagingTemplate.convertAndSend(destination, event);
        log.info("Published assignment progress to {} for assignment {}", destination, event.getAssignmentId());
    }

    public void publishBookingStatus(BookingStatusWebSocketEvent event) {
        if (event == null || event.getBookingId() == null) {
            log.warn("Skip publishing booking status because event or bookingId is null");
            return;
        }

        String destination = String.format("/topic/bookings/%s/status", event.getBookingId());
        messagingTemplate.convertAndSend(destination, event);
        log.info("Published booking status {} to {}", event.getStatus(), destination);
    }
}
