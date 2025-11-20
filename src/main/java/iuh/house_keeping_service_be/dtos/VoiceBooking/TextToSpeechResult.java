package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TextToSpeechResult(
        String text,
        String audioUrl,
        String provider,
        Long processingTimeMs
) {
}
