package iuh.house_keeping_service_be.dtos.Chat.request;

import iuh.house_keeping_service_be.enums.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatMessagePayload(
        @NotBlank(message = "Chat room id is required") String chatRoomId,
        @NotNull(message = "Message type is required") ChatMessageType messageType,
        String content,
        String attachmentUrl,
        String replyToMessageId
) {
}