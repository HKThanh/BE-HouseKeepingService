package iuh.house_keeping_service_be.services.ChatService;

import iuh.house_keeping_service_be.dtos.Chat.ChatMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(ChatMessageRequest request);
    
    ChatMessageResponse sendTextMessage(String conversationId, String senderId, String content);
    
    ChatMessageResponse sendImageMessage(String conversationId, String senderId, MultipartFile imageFile);
    
    Page<ChatMessageResponse> getMessagesByConversation(String conversationId, Pageable pageable);
    
    List<ChatMessageResponse> getAllMessagesByConversation(String conversationId);
    
    Long getUnreadMessageCount(String conversationId, String accountId);
    
    void markMessagesAsRead(String conversationId, String accountId);
}
