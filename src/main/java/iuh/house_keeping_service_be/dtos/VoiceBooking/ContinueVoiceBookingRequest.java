package iuh.house_keeping_service_be.dtos.VoiceBooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for continuing a partial voice booking with additional information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContinueVoiceBookingRequest {
    
    /**
     * Original voice booking request ID (from partial response)
     */
    private String requestId;
    
    /**
     * Additional audio file with missing information (optional)
     */
    private MultipartFile audio;
    
    /**
     * Text transcript of additional information (optional, alternative to audio)
     */
    private String additionalText;
    
    /**
     * Explicit field values to fill missing information (optional)
     * E.g., {"address": "123 Nguyen Van A", "district": "Quan 1"}
     */
    private java.util.Map<String, String> explicitFields;
    
    /**
     * Validate that at least one source of additional info is provided
     */
    public void validate() {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId is required");
        }
        
        boolean hasAudio = audio != null && !audio.isEmpty();
        boolean hasText = additionalText != null && !additionalText.isBlank();
        boolean hasExplicit = explicitFields != null && !explicitFields.isEmpty();
        
        if (!hasAudio && !hasText && !hasExplicit) {
            throw new IllegalArgumentException(
                "At least one of audio, additionalText, or explicitFields must be provided"
            );
        }
    }
}
