package iuh.house_keeping_service_be.services.ChatService.impl;

import com.cloudinary.Cloudinary;
import iuh.house_keeping_service_be.dtos.Chat.ChatMessageRequest;
import iuh.house_keeping_service_be.dtos.Chat.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.ChatMessageWebSocketDTO;
import iuh.house_keeping_service_be.enums.MessageType;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.ChatMessage;
import iuh.house_keeping_service_be.models.Conversation;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.ChatMessageRepository;
import iuh.house_keeping_service_be.repositories.ConversationRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
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
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
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
            
            // Validate file type
            String contentType = request.getImageFile().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File phải là định dạng ảnh");
            }
            
            // Validate file size (max 10MB)
            if (request.getImageFile().getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Kích thước file không được vượt quá 10MB");
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
    public Long getUnreadMessageCount(String conversationId, String receiverId) {
        return chatMessageRepository.countUnreadMessages(conversationId, receiverId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String conversationId, String receiverId) {
        chatMessageRepository.markAllAsRead(conversationId, receiverId);
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
        String senderAvatar = getSenderAvatar(message.getSender());
        
        return ChatMessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation().getConversationId())
                .senderId(message.getSender().getAccountId())
                .senderName(senderName)
                .senderAvatar(senderAvatar)
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

    private String getSenderAvatar(Account sender) {
        // Try to get avatar from Customer first
        Customer customer = customerRepository.findByAccount_AccountId(sender.getAccountId()).orElse(null);
        if (customer != null) {
            return customer.getAvatar();
        }
        
        // If not customer, try Employee
        Employee employee = employeeRepository.findByAccount_AccountId(sender.getAccountId()).orElse(null);
        if (employee != null) {
            return employee.getAvatar();
        }
        
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadMessageCountBySenderId(String senderId) {
        return chatMessageRepository.countUnreadMessagesBySenderId(senderId);
    }

    @Override
    @Transactional
    public int markMessagesAsReadBySenderIdAndConversation(String senderId, String conversationId) {
        return chatMessageRepository.markAsReadBySenderIdAndConversation(senderId, conversationId);
    }

    @Override
    @Transactional
    public int markAllMessagesAsReadBySenderId(String senderId) {
        return chatMessageRepository.markAllAsReadBySenderId(senderId);
    }

    @Override
    @Transactional
    public ChatMessageResponse saveMessageFromWebSocket(ChatMessageWebSocketDTO message) {
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Account sender = accountRepository.findById(message.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        MessageType messageType = message.getMessageType() != null
                ? message.getMessageType()
                : MessageType.TEXT;

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(conversation);
        chatMessage.setSender(sender);
        chatMessage.setMessageType(messageType);

        if (messageType == MessageType.TEXT) {
            if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Content is required for text messages");
            }
            chatMessage.setContent(message.getContent());
        } else if (messageType == MessageType.IMAGE) {
            if (message.getImageUrl() == null || message.getImageUrl().isBlank()) {
                throw new IllegalArgumentException("Image URL is required for image messages sent via WebSocket");
            }
            chatMessage.setImageUrl(message.getImageUrl());
            chatMessage.setContent(message.getContent());
        }

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        String lastMessagePreview = messageType == MessageType.TEXT
                ? chatMessage.getContent()
                : "[Image]";
        conversationService.updateLastMessage(conversation.getConversationId(), lastMessagePreview);

        return mapToResponse(savedMessage);
    }
}
