package iuh.house_keeping_service_be.dtos.Chat.response;

import java.util.List;

public record ConversationResponse(
        String conversationId,
        String bookingId,
        List<ChatParticipantDto> participants
) {
}
