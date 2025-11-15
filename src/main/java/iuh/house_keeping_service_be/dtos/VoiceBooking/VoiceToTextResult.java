package iuh.house_keeping_service_be.dtos.VoiceBooking;

import lombok.Builder;

import java.util.Map;

/**
 * Internal DTO for voice-to-text conversion result
 */
@Builder
public record VoiceToTextResult(
        String transcript,
        Double confidenceScore, // null if not available
        long processingTimeMs,
        String language, // Detected language
        Map<String, Object> metadata // Additional metadata from Whisper
) {
    public boolean hasTranscript() {
        return transcript != null && !transcript.isBlank();
    }

    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore >= 0.8;
    }
}
