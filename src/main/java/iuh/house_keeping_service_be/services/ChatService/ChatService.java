package iuh.house_keeping_service_be.services.ChatService;

import iuh.house_keeping_service_be.dtos.Chat.request.SendMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.response.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ConversationSummaryResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.MessagePageResponse;
import iuh.house_keeping_service_be.models.Conversation;

import java.util.List;

public interface ChatService {

    Conversation ensureConversation(String employeeId, String customerId);

    ChatMessageResponse sendMessage(String accountId, SendMessageRequest request);

    ChatMessageResponse recallMessage(String accountId, String messageId);

    List<ConversationSummaryResponse> getUserConversations(String accountId);

    MessagePageResponse getConversationMessages(String accountId, String conversationId, int page, int size);

    boolean isParticipant(String conversationId, String accountId);
}