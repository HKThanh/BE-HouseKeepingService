package iuh.house_keeping_service_be.dtos.ChatMessage.response;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        String chatMessageId,
        String chatRoomId,
        String senderAccountId,
        String messageText,
        String payloadType,
        String payloadData,
        LocalDateTime sentAt,
        LocalDateTime readAt,
        String readByAccountId,
        ReplyInfo reply,
        DeletionInfo deletion,
        RecallInfo recall
) {
    public record ReplyInfo(
            String messageId,
            String senderAccountId,
            String messageText
    ) {
    }

    public record DeletionInfo(
            LocalDateTime deletedAt,
            String deletedByAccountId
    ) {
    }

    public record RecallInfo(
            LocalDateTime recalledAt
    ) {
    }
}