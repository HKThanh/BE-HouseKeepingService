package iuh.house_keeping_service_be.services.ChatService.impl;

import com.cloudinary.Cloudinary;
import iuh.house_keeping_service_be.dtos.Chat.ChatMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.ChatMessageResponse;
import iuh.house_keeping_service_be.enums.MessageType;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.ChatMessage;
import iuh.house_keeping_service_be.models.Conversation;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.ChatMessageRepository;
import iuh.house_keeping_service_be.repositories.ConversationRepository;
import iuh.house_keeping_service_be.services.ChatService.ChatMessageService;
import iuh.house_keeping_service_be.services.ChatService.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final AccountRepository accountRepository;
    private final ConversationService conversationService;
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folders.chat:chat_images}")
    private String chatFolder;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Account sender = accountRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setMessageType(request.getMessageType());

        if (request.getMessageType() == MessageType.TEXT) {
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Content is required for text messages");
            }
            message.setContent(request.getContent());
        } else if (request.getMessageType() == MessageType.IMAGE) {
            if (request.getImageFile() == null || request.getImageFile().isEmpty()) {
                throw new IllegalArgumentException("Image file is required for image messages");
            }
            String imageUrl = uploadImage(request.getImageFile());
            message.setImageUrl(imageUrl);
            message.setContent(request.getContent()); // Optional caption
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Update conversation's last message
        String lastMessagePreview = message.getMessageType() == MessageType.TEXT 
                ? message.getContent() 
                : "[Image]";
        conversationService.updateLastMessage(conversation.getConversationId(), lastMessagePreview);

        return mapToResponse(savedMessage);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendTextMessage(String conversationId, String senderId, String content) {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setConversationId(conversationId);
        request.setSenderId(senderId);
        request.setMessageType(MessageType.TEXT);
        request.setContent(content);
        return sendMessage(request);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendImageMessage(String conversationId, String senderId, MultipartFile imageFile) {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setConversationId(conversationId);
        request.setSenderId(senderId);
        request.setMessageType(MessageType.IMAGE);
        request.setImageFile(imageFile);
        return sendMessage(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessagesByConversation(String conversationId, Pageable pageable) {
        Page<ChatMessage> messages = chatMessageRepository.findByConversation_ConversationIdOrderByCreatedAtDesc(
                conversationId, pageable);
        return messages.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getAllMessagesByConversation(String conversationId) {
        List<ChatMessage> messages = chatMessageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(
                conversationId);
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(String conversationId, String accountId) {
        return chatMessageRepository.countUnreadMessages(conversationId, accountId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String conversationId, String accountId) {
        chatMessageRepository.markAllAsRead(conversationId, accountId);
    }

    private String uploadImage(MultipartFile file) {
        try {
            Map<String, Object> options = new HashMap<>();
            if (chatFolder != null && !chatFolder.isBlank()) {
                options.put("folder", chatFolder);
            }
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new IllegalStateException("Failed to upload image to Cloudinary");
            }
            return secureUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        String senderName = getSenderName(message.getSender());
        
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation().getConversationId())
                .senderId(message.getSender().getAccountId())
                .senderName(senderName)
                .messageType(message.getMessageType())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private String getSenderName(Account sender) {
        // Try to get name from Customer
        return accountRepository.findById(sender.getAccountId())
                .map(account -> account.getUsername())
                .orElse("Unknown");
    }
}
