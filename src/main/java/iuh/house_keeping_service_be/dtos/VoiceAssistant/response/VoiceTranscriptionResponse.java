package iuh.house_keeping_service_be.dtos.VoiceAssistant.response;

public record VoiceTranscriptionResponse(
        boolean success,
        String transcription,
        String message,
        Long processingTimeMs
) {
}
