package iuh.house_keeping_service_be.config.websocket;

import iuh.house_keeping_service_be.services.ChatService.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatChannelInterceptor implements ChannelInterceptor {

    private static final String CONVERSATION_TOPIC_PREFIX = "/topic/conversations/";

    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new AccessDeniedException("Không thể xác thực người dùng WebSocket");
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith(CONVERSATION_TOPIC_PREFIX)) {
                String conversationId = destination.substring(CONVERSATION_TOPIC_PREFIX.length());
                if (!chatService.isParticipant(conversationId, principal.getName())) {
                    log.warn("Account {} attempted to subscribe to unauthorized conversation {}", principal.getName(), conversationId);
                    throw new AccessDeniedException("Không có quyền truy cập cuộc hội thoại này");
                }
            }
        }
        return message;
    }
}