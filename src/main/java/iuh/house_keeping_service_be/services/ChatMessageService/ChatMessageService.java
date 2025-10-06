package iuh.house_keeping_service_be.services.ChatMessageService;

import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageDeleteRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageRecallRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageReplyRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageSendRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.response.ChatMessageResponse;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(String chatRoomId, ChatMessageSendRequest request);

    ChatMessageResponse replyMessage(String parentMessageId, ChatMessageReplyRequest request);

    ChatMessageResponse deleteMessage(String messageId, ChatMessageDeleteRequest request);

    ChatMessageResponse recallMessage(String messageId, ChatMessageRecallRequest request);
    
    // Method to allow dynamic joining of chat rooms for testing
    boolean addParticipantToChatRoom(String chatRoomId, String accountId);
}