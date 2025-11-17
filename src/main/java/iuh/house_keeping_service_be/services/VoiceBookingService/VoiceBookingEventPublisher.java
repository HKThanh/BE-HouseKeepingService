package iuh.house_keeping_service_be.services.VoiceBookingService;

import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceBookingErrorPayload;
import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceBookingEventPayload;
import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceBookingEventType;
import iuh.house_keeping_service_be.enums.VoiceBookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sends real-time events over STOMP to subscribers listening on /topic/voice-booking/{requestId}
 * and pushes connection/authentication errors to /user/queue/voice-booking/errors.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceBookingEventPublisher {

    private static final String TOPIC_TEMPLATE = "/topic/voice-booking/%s";
    private static final String ERROR_QUEUE = "/queue/voice-booking/errors";

    private final SimpMessagingTemplate messagingTemplate;

    public void emitReceived(String requestId, String username) {
        VoiceBookingEventPayload payload = buildPayload(
                requestId,
                VoiceBookingEventType.RECEIVED,
                VoiceBookingStatus.PROCESSING,
                builder -> {}
        );
        sendToTopic(username, payload);
    }

    public void emitTranscribing(String requestId, String username, double progress) {
        VoiceBookingEventPayload payload = buildPayload(
                requestId,
                VoiceBookingEventType.TRANSCRIBING,
                VoiceBookingStatus.PROCESSING,
                builder -> builder.progress(sanitizeProgress(progress))
        );
        sendToTopic(username, payload);
    }

    public void emitPartial(
            String requestId,
            String username,
            String transcript,
            List<String> missingFields,
            String clarificationMessage,
            Integer processingTimeMs
    ) {
        VoiceBookingEventPayload payload = buildPayload(
                requestId,
                VoiceBookingEventType.PARTIAL,
                VoiceBookingStatus.PARTIAL,
                builder -> builder
                        .transcript(transcript)
                        .missingFields(missingFields)
                        .clarificationMessage(clarificationMessage)
                        .processingTimeMs(processingTimeMs)
        );
        sendToTopic(username, payload);
    }

    public void emitCompleted(
            String requestId,
            String username,
            String bookingId,
            String transcript,
            Integer processingTimeMs
    ) {
        VoiceBookingEventPayload payload = buildPayload(
                requestId,
                VoiceBookingEventType.COMPLETED,
                VoiceBookingStatus.COMPLETED,
                builder -> builder
                        .bookingId(bookingId)
                        .transcript(transcript)
                        .processingTimeMs(processingTimeMs)
        );
        sendToTopic(username, payload);
    }

    public void emitFailed(
            String requestId,
            String username,
            String errorMessage,
            String transcript,
            Integer processingTimeMs
    ) {
        VoiceBookingEventPayload payload = buildPayload(
                requestId,
                VoiceBookingEventType.FAILED,
                VoiceBookingStatus.FAILED,
                builder -> builder
                        .errorMessage(errorMessage)
                        .transcript(transcript)
                        .processingTimeMs(processingTimeMs)
        );
        sendToTopic(username, payload);
    }

    public void publishConnectionError(String username, String requestId, String errorMessage, String errorCode) {
        VoiceBookingErrorPayload payload = VoiceBookingErrorPayload.builder()
                .errorCode(errorCode != null ? errorCode : "VOICE_BOOKING_CONNECTION_ERROR")
                .errorMessage(errorMessage)
                .requestId(requestId)
                .timestamp(Instant.now())
                .build();

        try {
            messagingTemplate.convertAndSendToUser(username, ERROR_QUEUE, payload);
            log.warn("[VoiceBooking][WS][ERROR] user={} requestId={} errorCode={} message={}",
                    username, requestId, payload.errorCode(), payload.errorMessage());
        } catch (Exception ex) {
            log.error("Failed to publish voice booking connection error to user {}: {}", username, ex.getMessage(), ex);
        }
    }

    private VoiceBookingEventPayload buildPayload(
            String requestId,
            VoiceBookingEventType eventType,
            VoiceBookingStatus status,
            Consumer<VoiceBookingEventPayload.VoiceBookingEventPayloadBuilder> customizer
    ) {
        VoiceBookingEventPayload.VoiceBookingEventPayloadBuilder builder = VoiceBookingEventPayload.builder()
                .eventType(eventType)
                .requestId(requestId)
                .status(status != null ? status.name() : null)
                .timestamp(Instant.now());

        if (customizer != null) {
            customizer.accept(builder);
        }

        return builder.build();
    }

    private void sendToTopic(String username, VoiceBookingEventPayload payload) {
        try {
            messagingTemplate.convertAndSend(String.format(TOPIC_TEMPLATE, payload.requestId()), payload);
            log.info("[VoiceBooking][WS] user={} request={} event={} status={} processingTimeMs={}",
                    username,
                    payload.requestId(),
                    payload.eventType(),
                    payload.status(),
                    payload.processingTimeMs());
        } catch (Exception ex) {
            log.error("Failed to publish voice booking event {} for request {}: {}",
                    payload.eventType(), payload.requestId(), ex.getMessage(), ex);
        }
    }

    private double sanitizeProgress(double progress) {
        if (Double.isNaN(progress) || Double.isInfinite(progress)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(progress, 1.0));
    }
}
