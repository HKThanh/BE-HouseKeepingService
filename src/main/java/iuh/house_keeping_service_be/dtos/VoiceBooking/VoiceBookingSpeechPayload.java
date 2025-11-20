package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VoiceBookingSpeechPayload(
        TextToSpeechResult message,
        TextToSpeechResult clarification
) {
    public boolean hasAudio() {
        return (message != null && message.audioUrl() != null) ||
                (clarification != null && clarification.audioUrl() != null);
    }
}
