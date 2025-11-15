package iuh.house_keeping_service_be.services.VoiceAssistantService;

import iuh.house_keeping_service_be.dtos.VoiceAssistant.internal.BookingIntentExtractionResult;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.internal.VoiceProcessingResult;
import iuh.house_keeping_service_be.dtos.VoiceAssistant.response.VoiceBookingResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VoiceAssistantService {
    /**
     * Process voice input and convert to text
     * @param audioFile Audio file containing voice input
     * @return VoiceProcessingResult with transcription
     */
    VoiceProcessingResult processVoiceToText(MultipartFile audioFile);
    
    /**
     * Extract booking intent from transcribed text
     * @param transcription Text transcription from voice
     * @param customerId Customer ID making the booking
     * @return BookingIntentExtractionResult with extracted booking information
     */
    BookingIntentExtractionResult extractBookingIntent(String transcription, String customerId);
    
    /**
     * Process complete voice booking flow
     * @param audioFile Audio file containing voice booking request
     * @param customerId Customer ID making the booking
     * @return VoiceBookingResponse with complete booking result
     */
    VoiceBookingResponse processVoiceBooking(MultipartFile audioFile, String customerId);
}
