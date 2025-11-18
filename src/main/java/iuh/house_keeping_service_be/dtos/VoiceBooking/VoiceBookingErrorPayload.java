package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

/**
 * Payload for /user/queue/voice-booking/errors channel.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceBookingErrorPayload(
        String errorCode,
        String errorMessage,
        String requestId,
        Instant timestamp
) {
}
