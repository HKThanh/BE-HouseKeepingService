package iuh.house_keeping_service_be.services.ConversationService;

import iuh.house_keeping_service_be.dtos.Chat.response.AttachmentUploadResponse;
import iuh.house_keeping_service_be.dtos.Chat.response.ConversationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ConversationService {
    ConversationResponse getOrCreateConversation(String bookingId, String username);

    AttachmentUploadResponse uploadAttachment(String conversationId, MultipartFile file, String username);

    boolean isParticipant(String conversationId, String accountId);
}