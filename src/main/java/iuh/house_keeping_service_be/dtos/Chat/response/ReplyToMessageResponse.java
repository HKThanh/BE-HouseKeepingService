package iuh.house_keeping_service_be.dtos.Chat.response;

import iuh.house_keeping_service_be.enums.MessageType;

public record ReplyToMessageResponse(
        String messageId,
        MessageType messageType,
        String content,
        boolean revoked
) {
}