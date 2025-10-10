package iuh.house_keeping_service_be.dtos.Chat.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AttachmentPayload(
        @NotBlank(message = "Attachment URL is required")
        String url,
        @NotBlank(message = "Attachment publicId is required")
        String publicId,
        @NotBlank(message = "Attachment content type is required")
        String contentType,
        @NotNull(message = "Attachment size is required")
        @Positive(message = "Attachment size must be positive")
        Long size
) {
}