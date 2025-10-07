package iuh.house_keeping_service_be.dtos.Chat.response;

import iuh.house_keeping_service_be.enums.ChatMessageType;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        String messageId,
        String conversationId,
        String senderAccountId,
        String senderDisplayName,
        String senderAvatar,
        String senderRole,
        ChatMessageType messageType,
        String content,
        String fileName,
        String fileUrl,
        Long fileSize,
        boolean revoked,
        ReplyMessageResponse replyTo,
        LocalDateTime sentAt,
        LocalDateTime updatedAt
) {
}