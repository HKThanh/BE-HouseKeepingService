package iuh.house_keeping_service_be.dtos.Chat.response;

import java.time.LocalDateTime;

public record ConversationSummaryResponse(
        String conversationId,
        ConversationParticipantResponse participant,
        ChatMessageResponse lastMessage,
        LocalDateTime lastMessageAt
) {
}