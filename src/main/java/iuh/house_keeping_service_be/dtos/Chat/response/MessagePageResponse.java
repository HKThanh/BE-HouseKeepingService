package iuh.house_keeping_service_be.dtos.Chat.response;

import java.util.List;

public record MessagePageResponse(
        List<ChatMessageResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}