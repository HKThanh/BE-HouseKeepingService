package iuh.house_keeping_service_be.dtos.Chat.response;

import iuh.house_keeping_service_be.enums.ChatParticipantType;

public record ChatParticipantDto(
        String participantId,
        String accountId,
        String username,
        ChatParticipantType type,
        String displayName,
        String avatarUrl
) {
}
