package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Payload pushed to clients listening on /topic/voice-booking/{requestId}.
 * Fields are nullable so the frontend can reuse the same DTO for all event types.
 */
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceBookingEventPayload(
        VoiceBookingEventType eventType,
        String requestId,
        String status,
        String message,
        String transcript,
        List<String> missingFields,
        String clarificationMessage,
        String bookingId,
        Integer processingTimeMs,
        String errorMessage,
        Boolean isFinal,
        Double confidence,
        List<String> failureHints,
        Integer retryAfterMs,
        VoiceBookingSpeechPayload speech,
        VoiceBookingPreview preview,
        Instant timestamp,
        Double progress
) {
    public VoiceBookingEventPayload withSpeech(VoiceBookingSpeechPayload speechPayload) {
        return this.toBuilder()
                .speech(speechPayload)
                .build();
    }
}
