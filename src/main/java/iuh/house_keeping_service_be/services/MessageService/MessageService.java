package iuh.house_keeping_service_be.services.MessageService;

import iuh.house_keeping_service_be.dtos.Chat.request.ChatMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageResponse sendMessage(ChatMessageRequest request, String username);

    Page<MessageResponse> getConversationMessages(String conversationId, Pageable pageable, String username);

    MessageResponse recallMessage(String messageId, String username);
}