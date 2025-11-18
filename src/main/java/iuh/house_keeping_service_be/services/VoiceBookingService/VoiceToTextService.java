package iuh.house_keeping_service_be.services.VoiceBookingService;

import iuh.house_keeping_service_be.dtos.VoiceBooking.VoiceToTextResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service interface for voice-to-text conversion
 * Supports multiple implementations (Whisper API, Whisper.cpp, etc.)
 */
public interface VoiceToTextService {

    /**
     * Convert audio file to text
     * @param audioFile The audio file to transcribe
     * @param language Optional language hint (e.g., "vi" for Vietnamese)
     * @return VoiceToTextResult containing transcript and metadata
     * @throws IOException if audio processing fails
     */
    VoiceToTextResult transcribe(MultipartFile audioFile, String language) throws IOException;

    /**
     * Check if the service is enabled
     */
    boolean isEnabled();

    /**
     * Validate audio file before processing
     * @throws IllegalArgumentException if validation fails
     */
    void validateAudioFile(MultipartFile audioFile);
}
