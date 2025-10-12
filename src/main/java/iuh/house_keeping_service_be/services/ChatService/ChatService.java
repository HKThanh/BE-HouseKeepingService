package iuh.house_keeping_service_be.services.ChatService;

import iuh.house_keeping_service_be.dtos.Chat.request.ChatMessagePayload;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {
    ChatRoomResponse createOrGetChatRoom(String bookingId, String accountId);

    ChatRoomResponse getChatRoomByBooking(String bookingId, String accountId);

    Page<ChatMessageResponse> getMessages(String chatRoomId, Pageable pageable, String accountId);

    ChatMessageResponse sendMessage(ChatMessagePayload payload, String accountId);

    ChatMessageResponse revokeMessage(String messageId, String accountId);
}