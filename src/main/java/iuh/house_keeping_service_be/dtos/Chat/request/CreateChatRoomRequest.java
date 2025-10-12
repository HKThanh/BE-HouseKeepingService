package iuh.house_keeping_service_be.dtos.Chat.request;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRoomRequest(
        @NotBlank(message = "Booking id is required") String bookingId
) {
}
