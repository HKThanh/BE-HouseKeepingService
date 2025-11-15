package iuh.house_keeping_service_be.dtos.VoiceBooking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Request DTO for voice booking creation
 * Receives audio file and optional hints for better parsing
 */
public record VoiceBookingRequest(
        @NotNull(message = "Audio file is required")
        MultipartFile audio,

        @Size(max = 1000, message = "Hints cannot exceed 1000 characters")
        String hints // Optional JSON string with context hints
) {
    // Validation for audio file
    public void validateAudio() {
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file cannot be empty");
        }

        // Check file size (max 5MB)
        long maxSizeBytes = 5 * 1024 * 1024; // 5MB
        if (audio.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("Audio file size exceeds 5MB limit");
        }

        // Check content type
        String contentType = audio.getContentType();
        if (contentType == null || (!contentType.startsWith("audio/") && !contentType.equals("application/octet-stream"))) {
            throw new IllegalArgumentException("File must be audio format");
        }
    }

    /**
     * Parse hints JSON string to Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseHints() {
        if (hints == null || hints.isBlank()) {
            return Map.of();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(hints, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
