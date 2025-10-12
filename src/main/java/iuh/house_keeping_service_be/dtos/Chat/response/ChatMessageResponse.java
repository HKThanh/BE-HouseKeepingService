package iuh.house_keeping_service_be.dtos.Chat.response;

import iuh.house_keeping_service_be.enums.ChatMessageType;
import iuh.house_keeping_service_be.enums.ChatParticipantRole;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        String messageId,
        String chatRoomId,
        String senderAccountId,
        ChatParticipantRole senderRole,
        String senderDisplayName,
        ChatMessageType messageType,
        String content,
        String attachmentUrl,
        boolean isRevoke,
        ReplyMessageSummary replyTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record ReplyMessageSummary(
            String messageId,
            ChatMessageType messageType,
            String preview,
            boolean isRevoke
    ) {
    }
}