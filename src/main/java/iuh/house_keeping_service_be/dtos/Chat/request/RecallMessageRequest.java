package iuh.house_keeping_service_be.dtos.Chat.request;

import jakarta.validation.constraints.NotBlank;

public record RecallMessageRequest(
        @NotBlank(message = "messageId is required")
        String messageId
) {
}
