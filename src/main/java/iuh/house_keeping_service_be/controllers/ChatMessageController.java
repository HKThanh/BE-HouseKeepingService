package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Chat.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.ChatMessageWebSocketDTO;
import iuh.house_keeping_service_be.dtos.Chat.ConversationResponse;
import iuh.house_keeping_service_be.dtos.Chat.ConversationWebSocketDTO;
import iuh.house_keeping_service_be.services.ChatService.ChatMessageService;
import iuh.house_keeping_service_be.services.ChatService.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@Slf4j
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send/text")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> sendTextMessage(
            @RequestParam String conversationId,
            @RequestParam String senderId,
            @RequestParam String content) {
        try {
            ChatMessageResponse response = chatMessageService.sendTextMessage(conversationId, senderId, content);
            
            ChatMessageWebSocketDTO wsMessage = buildWebSocketMessage(response);
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, wsMessage);
            broadcastConversationSummary(conversationId, response.getSenderId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Message sent successfully",
                    "data", response
            ));
        } catch (Exception e) {
            log.error("Error sending text message: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to send message: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/send/image")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> sendImageMessage(
            @RequestParam String conversationId,
            @RequestParam String senderId,
            @RequestParam MultipartFile imageFile,
            @RequestParam(required = false) String caption) {
        try {
            ChatMessageResponse response = chatMessageService.sendImageMessage(conversationId, senderId, imageFile);
            
            ChatMessageWebSocketDTO wsMessage = buildWebSocketMessage(response, caption);
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, wsMessage);
            broadcastConversationSummary(conversationId, response.getSenderId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Image sent successfully",
                    "data", response
            ));
        } catch (Exception e) {
            log.error("Error sending image message: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to send image: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getMessagesByConversation(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ChatMessageResponse> messages = chatMessageService.getMessagesByConversation(conversationId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages.getContent());
            response.put("currentPage", messages.getNumber());
            response.put("totalItems", messages.getTotalElements());
            response.put("totalPages", messages.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting messages: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get messages: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/conversation/{conversationId}/all")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAllMessagesByConversation(@PathVariable String conversationId) {
        try {
            List<ChatMessageResponse> messages = chatMessageService.getAllMessagesByConversation(conversationId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", messages
            ));
        } catch (Exception e) {
            log.error("Error getting all messages: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get messages: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/conversation/{conversationId}/unread-count")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getUnreadMessageCount(
            @PathVariable String conversationId,
            @RequestParam String receiverId) {
        try {
            Long count = chatMessageService.getUnreadMessageCount(conversationId, receiverId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "receiverId", receiverId,
                            "conversationId", conversationId,
                            "unreadCount", count
                    )
            ));
        } catch (Exception e) {
            log.error("Error getting unread count: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get unread count: " + e.getMessage()
            ));
        }
    }

    // New endpoints using receiverId (customerId or employeeId) - query parameter format
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getUnreadMessageCountByReceiverId(@RequestParam String receiverId) {
        try {
            Long count = chatMessageService.getUnreadMessageCountBySenderId(receiverId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "receiverId", receiverId,
                            "unreadCount", count
                    )
            ));
        } catch (Exception e) {
            log.error("Error getting unread count for receiverId {}: ", receiverId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get unread count: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/conversation/{conversationId}/mark-read")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> markMessagesAsReadByReceiverIdAndConversation(
            @PathVariable String conversationId,
            @RequestParam String receiverId) {
        try {
            int updatedCount = chatMessageService.markMessagesAsReadBySenderIdAndConversation(receiverId, conversationId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Messages marked as read",
                    "data", Map.of(
                            "receiverId", receiverId,
                            "conversationId", conversationId,
                            "markedCount", updatedCount
                    )
            ));
        } catch (Exception e) {
            log.error("Error marking messages as read for receiverId {} in conversation {}: ", receiverId, conversationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to mark messages as read: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/mark-all-read")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> markAllMessagesAsReadByReceiverId(@RequestParam String receiverId) {
        try {
            int updatedCount = chatMessageService.markAllMessagesAsReadBySenderId(receiverId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "All messages marked as read",
                    "data", Map.of(
                            "receiverId", receiverId,
                            "markedCount", updatedCount
                    )
            ));
        } catch (Exception e) {
            log.error("Error marking all messages as read for receiverId {}: ", receiverId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to mark all messages as read: " + e.getMessage()
            ));
        }
    }

    // WebSocket endpoint for real-time messaging
    @MessageMapping("/chat.send")
    public void sendMessageViaWebSocket(@Payload ChatMessageWebSocketDTO message) {
        try {
            ChatMessageResponse savedMessage = chatMessageService.saveMessageFromWebSocket(message);
            ChatMessageWebSocketDTO wsMessage = buildWebSocketMessage(savedMessage, message.getContent());

            messagingTemplate.convertAndSend("/topic/conversation/" + wsMessage.getConversationId(), wsMessage);
            broadcastConversationSummary(wsMessage.getConversationId(), wsMessage.getSenderId());
        } catch (Exception e) {
            log.error("Error sending WebSocket message: ", e);
        }
    }

    private ChatMessageWebSocketDTO buildWebSocketMessage(ChatMessageResponse response) {
        return buildWebSocketMessage(response, response.getContent());
    }

    private ChatMessageWebSocketDTO buildWebSocketMessage(ChatMessageResponse response, String overrideContent) {
        return ChatMessageWebSocketDTO.builder()
                .messageId(response.getMessageId())
                .conversationId(response.getConversationId())
                .senderId(response.getSenderId())
                .senderName(response.getSenderName())
                .senderAvatar(response.getSenderAvatar())
                .messageType(response.getMessageType())
                .content(overrideContent != null ? overrideContent : response.getContent())
                .imageUrl(response.getImageUrl())
                .timestamp(response.getCreatedAt())
                .build();
    }

    private void broadcastConversationSummary(String conversationId, String senderId) {
        try {
            ConversationResponse conversation = conversationService.getConversationById(conversationId);
            sendConversationSummaryToParticipant(conversation.getCustomerId(), conversation, senderId);
            if (conversation.getEmployeeId() != null) {
                sendConversationSummaryToParticipant(conversation.getEmployeeId(), conversation, senderId);
            }
        } catch (Exception e) {
            log.error("Failed to broadcast conversation summary for {}: {}", conversationId, e.getMessage());
        }
    }

    private void sendConversationSummaryToParticipant(
            String participantId,
            ConversationResponse conversation,
            String senderId
    ) {
        if (participantId == null) {
            return;
        }

        Long unreadCount = chatMessageService.getUnreadMessageCountBySenderId(participantId);
        ConversationWebSocketDTO payload = ConversationWebSocketDTO.builder()
                .conversationId(conversation.getConversationId())
                .participantId(participantId)
                .senderId(senderId)
                .lastMessage(conversation.getLastMessage())
                .lastMessageTime(conversation.getLastMessageTime())
                .unreadCount(unreadCount)
                .build();

        messagingTemplate.convertAndSend("/topic/conversation/summary/" + participantId, payload);
    }
}
