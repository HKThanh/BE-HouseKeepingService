package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * Metadata describing the synthesized speech that accompanies a voice booking response.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceBookingSpeech(
        String provider,
        String voice,
        String speed,
        String audioUrl,
        String requestId,
        String spokenText
) {
}
