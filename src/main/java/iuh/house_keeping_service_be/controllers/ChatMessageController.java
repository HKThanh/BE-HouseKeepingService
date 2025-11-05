package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Chat.ChatMessageResponse;
import iuh.house_keeping_service_be.dtos.Chat.ChatMessageWebSocketDTO;
import iuh.house_keeping_service_be.services.ChatService.ChatMessageService;
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
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send/text")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> sendTextMessage(
            @RequestParam String conversationId,
            @RequestParam String senderId,
            @RequestParam String content) {
        try {
            ChatMessageResponse response = chatMessageService.sendTextMessage(conversationId, senderId, content);
            
            // Send WebSocket notification
            ChatMessageWebSocketDTO wsMessage = ChatMessageWebSocketDTO.builder()
                    .messageId(response.getMessageId())
                    .conversationId(response.getConversationId())
                    .senderId(response.getSenderId())
                    .senderName(response.getSenderName())
                    .senderAvatar(response.getSenderAvatar())
                    .messageType(response.getMessageType())
                    .content(response.getContent())
                    .timestamp(response.getCreatedAt())
                    .build();
            
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, wsMessage);
            
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
            
            // Send WebSocket notification
            ChatMessageWebSocketDTO wsMessage = ChatMessageWebSocketDTO.builder()
                    .messageId(response.getMessageId())
                    .conversationId(response.getConversationId())
                    .senderId(response.getSenderId())
                    .senderName(response.getSenderName())
                    .senderAvatar(response.getSenderAvatar())
                    .messageType(response.getMessageType())
                    .imageUrl(response.getImageUrl())
                    .content(caption)
                    .timestamp(response.getCreatedAt())
                    .build();
            
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, wsMessage);
            
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
            // Broadcast to the conversation topic
            messagingTemplate.convertAndSend("/topic/conversation/" + message.getConversationId(), message);
        } catch (Exception e) {
            log.error("Error sending WebSocket message: ", e);
        }
    }
}
