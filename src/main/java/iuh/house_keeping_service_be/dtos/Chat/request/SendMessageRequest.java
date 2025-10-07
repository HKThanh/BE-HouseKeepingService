package iuh.house_keeping_service_be.dtos.Chat.request;

import iuh.house_keeping_service_be.enums.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank(message = "conversationId is required")
        String conversationId,

        @NotNull(message = "message type is required")
        ChatMessageType messageType,

        @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
        String content,

        String fileName,

        String fileUrl,

        Long fileSize,

        String replyToMessageId
) {
}