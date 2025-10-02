package iuh.house_keeping_service_be.dtos.ChatMessage.request;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRecallRequest(
        @NotBlank(message = "Sender account id is required")
        String accountId
) {
}