package iuh.house_keeping_service_be.dtos.VoiceAssistant.request;

import org.springframework.web.multipart.MultipartFile;

public record VoiceBookingRequest(
        MultipartFile audioFile,
        String customerId
) {
}
