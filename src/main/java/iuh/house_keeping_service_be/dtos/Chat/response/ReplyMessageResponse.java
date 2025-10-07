package iuh.house_keeping_service_be.dtos.Chat.response;

import iuh.house_keeping_service_be.enums.ChatMessageType;

import java.time.LocalDateTime;

public record ReplyMessageResponse(
        String messageId,
        ChatMessageType messageType,
        String content,
        String fileName,
        String fileUrl,
        boolean revoked,
        LocalDateTime sentAt
) {
}
