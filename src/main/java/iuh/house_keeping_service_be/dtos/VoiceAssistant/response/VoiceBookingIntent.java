package iuh.house_keeping_service_be.dtos.VoiceAssistant.response;

import java.time.LocalDateTime;
import java.util.List;

public record VoiceBookingIntent(
        String serviceType,
        LocalDateTime bookingTime,
        String address,
        String note,
        List<String> detectedServices,
        Double confidence
) {
}
