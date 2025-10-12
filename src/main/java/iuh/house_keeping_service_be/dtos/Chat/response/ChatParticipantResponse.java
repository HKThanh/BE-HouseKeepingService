package iuh.house_keeping_service_be.dtos.Chat.response;

import iuh.house_keeping_service_be.enums.ChatParticipantRole;

public record ChatParticipantResponse(
        String accountId,
        ChatParticipantRole role,
        String displayName,
        String avatarUrl
) {
}