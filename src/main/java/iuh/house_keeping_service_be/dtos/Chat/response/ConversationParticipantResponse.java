package iuh.house_keeping_service_be.dtos.Chat.response;

public record ConversationParticipantResponse(
        String accountId,
        String profileId,
        String fullName,
        String avatar,
        String role
) {
}