package iuh.house_keeping_service_be.dtos.Chat.response;

import java.time.LocalDateTime;

public record AttachmentResponse(
        String attachmentId,
        String url,
        String contentType,
        Long size,
        String publicId,
        LocalDateTime createdAt
) {
}
