package iuh.house_keeping_service_be.dtos.VoiceAssistant.internal;

public record VoiceProcessingResult(
        boolean success,
        String transcription,
        String errorMessage
) {
}
