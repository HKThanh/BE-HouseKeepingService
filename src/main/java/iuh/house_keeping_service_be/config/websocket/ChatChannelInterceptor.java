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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatChannelInterceptor implements ChannelInterceptor {

    private static final String CONVERSATION_TOPIC_PREFIX = "/topic/conversations/";

    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        log.info("Command: {}, Destination: {}, User: {}",
                accessor != null ? accessor.getCommand() : "N/A",
                accessor != null ? accessor.getDestination() : "N/A",
                accessor != null && accessor.getUser() != null ? accessor.getUser().getName() : "N/A");

        if (accessor == null) {
            return message;
        }

        Principal principal = accessor.getUser();
        if (principal == null) {
            principal = restorePrincipalFromSession(accessor);
        }

        if (principal == null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                log.warn("CONNECT frame arrived without authenticated principal");
                throw new AccessDeniedException("Không thể xác thực người dùng WebSocket");
            }
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

    private Principal restorePrincipalFromSession(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }

        Object accountId = sessionAttributes.get("accountId");
        Object username = sessionAttributes.get("username");
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) sessionAttributes.getOrDefault("roles", Collections.emptySet());

        if (accountId instanceof String accountIdStr && username instanceof String usernameStr) {
            StompPrincipal stompPrincipal = new StompPrincipal(accountIdStr, usernameStr, roles);
            accessor.setUser(stompPrincipal);
            return stompPrincipal;
        }
        return null;
    }
}