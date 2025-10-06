package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.ChatMessage.request.ChatMessageSendRequest;
import iuh.house_keeping_service_be.dtos.ChatMessage.response.ChatMessageResponse;
import iuh.house_keeping_service_be.exceptions.ResourceNotFoundException;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.ChatRoom;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.ChatRoomRepository;
import iuh.house_keeping_service_be.services.ChatMessageService.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;

/**
 * WebSocket Controller for handling real-time chat messaging
 * This controller handles STOMP messages for chat functionality
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepository accountRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * Handle sending messages via WebSocket
     * Endpoint: /app/chat/send
     */
    @MessageMapping("/chat/send")
    public void sendMessage(@Payload @Valid SimpleMessage message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("WebSocket message send request for chat room: {}", message.chatRoomId());
        
        try {
            // Get user from WebSocket session
            String username = getUserFromSession(headerAccessor);
            if (username == null) {
                log.warn("User not authenticated for message send, session: {}", headerAccessor.getSessionId());
                // Don't throw exception, just send error response
                messagingTemplate.convertAndSendToUser(
                    headerAccessor.getSessionId(), 
                    "/queue/errors", 
                    new ErrorMessage("Authentication required")
                );
                return;
            }

            log.debug("WebSocket message from user: {}", username);

            Account sender = accountRepository.findByUsername(username).orElse(null);
            if (sender == null) {
                log.warn("Account not found for username: {}", username);
                messagingTemplate.convertAndSendToUser(
                    username, 
                    "/queue/errors", 
                    new ErrorMessage("Account not found: " + username)
                );
                return;
            }

            // Validate chat room exists
            ChatRoom chatRoom = chatRoomRepository.findById(message.chatRoomId()).orElse(null);
            if (chatRoom == null) {
                log.warn("Chat room not found: {}", message.chatRoomId());
                messagingTemplate.convertAndSendToUser(
                    username, 
                    "/queue/errors", 
                    new ErrorMessage("Chat room not found: " + message.chatRoomId())
                );
                return;
            }
            
            log.debug("ChatRoom info - Customer: {}, Employee: {}", 
                     chatRoom.getCustomerAccount() != null ? chatRoom.getCustomerAccount().getAccountId() : "null",
                     chatRoom.getEmployeeAccount() != null ? chatRoom.getEmployeeAccount().getAccountId() : "null");

            // Create and send message using the service
            ChatMessageSendRequest serviceRequest = new ChatMessageSendRequest(
                    sender.getAccountId(),
                    message.content(),
                    null, // payloadType
                    null, // payloadData
                    null  // parentMessageId
            );

            ChatMessageResponse response = chatMessageService.sendMessage(message.chatRoomId(), serviceRequest);
            
            // The service will handle broadcasting via SimpMessagingTemplate
            log.info("WebSocket message sent successfully: {}", response.chatMessageId());
            
        } catch (Exception e) {
            log.error("Failed to send WebSocket message: {}", e.getMessage(), e);
            
            // Send error without throwing exception to prevent disconnection
            try {
                String username = getUserFromSession(headerAccessor);
                if (username != null) {
                    messagingTemplate.convertAndSendToUser(
                        username, 
                        "/queue/errors", 
                        new ErrorMessage("Failed to send message: " + e.getMessage())
                    );
                } else {
                    // Send to session if username not available
                    messagingTemplate.convertAndSendToUser(
                        headerAccessor.getSessionId(), 
                        "/queue/errors", 
                        new ErrorMessage("Failed to send message: " + e.getMessage())
                    );
                }
            } catch (Exception errorException) {
                log.error("Failed to send error message: {}", errorException.getMessage());
            }
        }
    }

    /**
     * Handle joining a chat room (subscription notification)
     * Endpoint: /app/chat/join/{chatRoomId}
     */
    @MessageMapping("/chat/join/{chatRoomId}")
    public void joinChatRoom(@DestinationVariable String chatRoomId, SimpMessageHeaderAccessor headerAccessor) {
        log.info("Processing join request for chat room: {} from session: {}", chatRoomId, headerAccessor.getSessionId());
        
        // Debug: Log detailed session information
        log.debug("Session attributes at join time: {}", headerAccessor.getSessionAttributes().keySet());
        headerAccessor.getSessionAttributes().forEach((key, value) -> {
            log.debug("  Session attribute - {}: {} (type: {})", key, value, value != null ? value.getClass().getSimpleName() : "null");
        });
        
        String username = getUserFromSession(headerAccessor);
        log.info("Extracted username: {} for chat room join: {}", username, chatRoomId);
        
        try {
            if (username == null) {
                log.error("User not authenticated for session: {}", headerAccessor.getSessionId());
                throw new SecurityException("User not authenticated");
            }

            // Validate chat room exists
            chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng chat: " + chatRoomId));

            // Optional: Send a notification that user joined (if needed)
            // For now, just log the join event
            log.info("User {} successfully joined chat room: {}", username, chatRoomId);
            
        } catch (Exception e) {
            log.error("Failed to join chat room {} for session {}: {}", chatRoomId, headerAccessor.getSessionId(), e.getMessage(), e);
            if (username != null) {
                sendErrorToUser(username, "Failed to join chat room: " + e.getMessage());
            }
        }
    }

    /**
     * Handle leaving a chat room
     * Endpoint: /app/chat/leave/{chatRoomId}
     */
    @MessageMapping("/chat/leave/{chatRoomId}")
    public void leaveChatRoom(@DestinationVariable String chatRoomId, SimpMessageHeaderAccessor headerAccessor) {
        String username = getUserFromSession(headerAccessor);
        log.info("User {} leaving chat room: {}", username, chatRoomId);
        
        // Optional: Send a notification that user left (if needed)
        // For now, just log the leave event
        log.info("User {} left chat room: {}", username, chatRoomId);
    }

    /**
     * Handle typing indicators
     * Endpoint: /app/chat/typing/{chatRoomId}
     */
    @MessageMapping("/chat/typing/{chatRoomId}")
    public void handleTyping(@DestinationVariable String chatRoomId, 
                           @Payload TypingIndicator typingIndicator,
                           SimpMessageHeaderAccessor headerAccessor) {
        String username = getUserFromSession(headerAccessor);
        log.debug("Typing indicator for chat room {}: {} is {}", 
                 chatRoomId, username, typingIndicator.isTyping() ? "typing" : "stopped typing");
        
        try {
            if (username == null) {
                throw new SecurityException("User not authenticated");
            }

            // Broadcast typing indicator to other participants
            TypingEvent typingEvent = new TypingEvent(
                    username,
                    typingIndicator.isTyping()
            );
            
            messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId + "/typing", typingEvent);
            
        } catch (Exception e) {
            log.error("Failed to handle typing indicator: {}", e.getMessage());
        }
    }

    /**
     * Send error message to specific user
     */
    private void sendErrorToUser(String username, String errorMessage) {
        try {
            ErrorMessage errorPayload = new ErrorMessage(errorMessage);
            
            messagingTemplate.convertAndSendToUser(username, "/queue/errors", errorPayload);
        } catch (Exception e) {
            log.error("Failed to send error message to user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Get username from WebSocket session with multiple fallback strategies
     */
    private String getUserFromSession(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();
            log.debug("Attempting to get user for session: {}", sessionId);
            
            // Strategy 1: Try to get user from the header accessor directly
            if (headerAccessor.getUser() != null) {
                String username = headerAccessor.getUser().getName();
                log.debug("Found username from headerAccessor.getUser(): {}", username);
                return username;
            }
            
            // Strategy 2: Try to get from session attributes with USERNAME key
            Object username = headerAccessor.getSessionAttributes().get("USERNAME");
            if (username instanceof String) {
                log.debug("Found username in session attributes with USERNAME key: {}", username);
                return (String) username;
            }
            
            // Strategy 3: Try to get from authenticated user in session
            Object authenticatedUser = headerAccessor.getSessionAttributes().get("AUTHENTICATED_USER");
            if (authenticatedUser instanceof Authentication) {
                String authUsername = ((Authentication) authenticatedUser).getName();
                log.debug("Found username from AUTHENTICATED_USER session attribute: {}", authUsername);
                return authUsername;
            }
            
            // Strategy 4: Try to extract from JWT token in session attributes
            Object jwtToken = headerAccessor.getSessionAttributes().get("JWT_TOKEN");
            if (jwtToken instanceof String) {
                try {
                    // You'll need to inject JwtUtil if this strategy is used
                    log.debug("Found JWT token in session, would need JwtUtil to extract username");
                    // return jwtUtil.extractUsername((String) jwtToken);
                } catch (Exception e) {
                    log.warn("Failed to extract username from JWT token: {}", e.getMessage());
                }
            }
            
            // Strategy 5: Try simpUser attribute (sometimes set by Spring Security)
            Object simpUser = headerAccessor.getSessionAttributes().get("simpUser");
            if (simpUser != null) {
                log.debug("Found simpUser in session attributes: {}", simpUser);
                if (simpUser instanceof Authentication) {
                    return ((Authentication) simpUser).getName();
                }
            }
            
            // Debug: log all session attributes to help troubleshooting
            log.debug("Session attributes available for session {}: {}", 
                     sessionId, headerAccessor.getSessionAttributes().keySet());
            
            // Log session attribute values for debugging
            headerAccessor.getSessionAttributes().forEach((key, value) -> {
                log.debug("Session attribute - {}: {} (type: {})", 
                         key, value, value != null ? value.getClass().getSimpleName() : "null");
            });
            
            log.warn("Could not extract username from WebSocket session for session: {}", sessionId);
            return null;
            
        } catch (Exception e) {
            log.error("Error extracting username from WebSocket session: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * DTO for error messages
     */
    public record ErrorMessage(String message) {}

    /**
     * DTO for typing indicator payload
     */
    public record TypingIndicator(boolean isTyping) {}

    /**
     * DTO for typing event broadcast
     */
    public record TypingEvent(String username, boolean isTyping) {}

    /**
     * DTO for simple WebSocket message (fallback)
     */
    public record SimpleMessage(String chatRoomId, String content) {}

    /**
     * DTO for echo message response
     */
    public record EchoMessage(String sender, String content) {}
}