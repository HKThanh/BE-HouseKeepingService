package iuh.house_keeping_service_be.services.VoiceBookingService;

import iuh.house_keeping_service_be.dtos.VoiceBooking.TextToSpeechResult;

import java.util.Optional;

public interface TextToSpeechService {
    Optional<TextToSpeechResult> synthesize(String text);

    boolean isEnabled();
}
