package iuh.house_keeping_service_be.dtos.ChatMessage.request;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageDeleteRequest(
        @NotBlank(message = "Actor account id is required")
        String accountId
) {
}