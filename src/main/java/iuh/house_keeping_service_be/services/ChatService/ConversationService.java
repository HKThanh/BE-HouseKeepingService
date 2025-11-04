package iuh.house_keeping_service_be.services.ChatService;

import iuh.house_keeping_service_be.dtos.Chat.ConversationRequest;
import iuh.house_keeping_service_be.dtos.Chat.ConversationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConversationService {
    ConversationResponse createConversation(ConversationRequest request);
    
    ConversationResponse getConversationById(String conversationId);
    
    Page<ConversationResponse> getConversationsByAccount(String accountId, Pageable pageable);
    
    ConversationResponse getOrCreateConversation(String customerId, String employeeId);
    
    ConversationResponse getConversationByBooking(String bookingId);
    
    void updateLastMessage(String conversationId, String lastMessage);
    
    void deleteConversation(String conversationId);
}
