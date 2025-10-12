package iuh.house_keeping_service_be.dtos.Chat.response;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomResponse(
        String chatRoomId,
        String bookingId,
        String bookingCode,
        List<ChatParticipantResponse> participants,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}