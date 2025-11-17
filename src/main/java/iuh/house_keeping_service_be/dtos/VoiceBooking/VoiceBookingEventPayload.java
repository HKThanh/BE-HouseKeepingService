package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Payload pushed to clients listening on /topic/voice-booking/{requestId}.
 * Fields are nullable so the frontend can reuse the same DTO for all event types.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceBookingEventPayload(
        VoiceBookingEventType eventType,
        String requestId,
        String status,
        String transcript,
        List<String> missingFields,
        String clarificationMessage,
        String bookingId,
        Integer processingTimeMs,
        String errorMessage,
        Instant timestamp,
        Double progress
) {
}
