package iuh.house_keeping_service_be.dtos.Chat.response;

import iuh.house_keeping_service_be.enums.MessageType;

import java.time.LocalDateTime;
import java.util.List;

public record MessageResponse(
        String messageId,
        String conversationId,
        ChatParticipantDto sender,
        MessageType messageType,
        String content,
        boolean revoked,
        ReplyToMessageResponse replyTo,
        List<AttachmentResponse> attachments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}