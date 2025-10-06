package iuh.house_keeping_service_be.security;

import iuh.house_keeping_service_be.security.CustomUserDetailsService;
import iuh.house_keeping_service_be.config.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    
    // Store authenticated users by session ID
    private final Map<String, Principal> authenticatedUsers = new ConcurrentHashMap<>();

    public ChatWebSocketAuthChannelInterceptor(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            
            log.debug("Processing STOMP command: {} for session: {}", 
                     accessor.getCommand(), accessor.getSessionId());
            
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                log.info("Processing CONNECT command for session: {}", accessor.getSessionId());
                authenticateUser(accessor);
                log.info("CONNECT authentication successful for session: {}", accessor.getSessionId());
            }

            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                log.info("Processing SUBSCRIBE command to: {} for session: {}", 
                        accessor.getDestination(), accessor.getSessionId());
                
                Principal userPrincipal = getUserForSession(accessor);
                
                if (userPrincipal == null) {
                    // Try to authenticate from header if not found in session
                    String authHeader = resolveAuthorizationHeader(accessor);
                    if (StringUtils.hasText(authHeader)) {
                        log.info("Found authorization header in SUBSCRIBE, re-authenticating");
                        authenticateUser(accessor);
                        userPrincipal = accessor.getUser();
                    }
                }
                
                if (userPrincipal == null) {
                    log.warn("Subscription denied for chat room {} due to missing user information", 
                            accessor.getDestination());
                    throw new AccessDeniedException("Không thể xác định người dùng hiện tại");
                }
                
                validateSubscription(accessor);
                log.info("SUBSCRIBE validation successful for user: {}", userPrincipal.getName());
            }

            if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                log.info("Processing DISCONNECT for session: {}", accessor.getSessionId());
                authenticatedUsers.remove(accessor.getSessionId());
            }

            return message;
            
        } catch (Exception ex) {
            log.error("Error in WebSocket interceptor: {}", ex.getMessage(), ex);
            return createErrorMessage(StompHeaderAccessor.wrap(message), ex.getMessage());
        }
    }

    private void authenticateUser(StompHeaderAccessor accessor) {
        String authHeader = resolveAuthorizationHeader(accessor);
        
        if (!StringUtils.hasText(authHeader)) {
            throw new SecurityException("Authorization header is required");
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid authorization header format");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        
        log.debug("Authenticating user: {} for session: {}", username, accessor.getSessionId());
        
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        
        if (!jwtUtil.validateToken(token, userDetails.getUsername())) {
            throw new SecurityException("Invalid or expired JWT token");
        }

        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
        // Set user in accessor
        accessor.setUser(authToken);
        
        // Store in session map for persistence across commands
        authenticatedUsers.put(accessor.getSessionId(), authToken);
        
        // Store multiple formats in session attributes for the controller to access
        accessor.getSessionAttributes().put("USERNAME", username);
        accessor.getSessionAttributes().put("AUTHENTICATED_USER", authToken);
        accessor.getSessionAttributes().put("JWT_TOKEN", token);
        accessor.getSessionAttributes().put("USER_DETAILS", userDetails);
        
        log.debug("Authentication successful for user: {}, session: {} - stored in session attributes", 
                 username, accessor.getSessionId());
        log.debug("Session attributes after auth: {}", accessor.getSessionAttributes().keySet());
    }

    private Principal getUserForSession(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            return authenticatedUsers.get(sessionId);
        }
        return null;
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        Principal userPrincipal = getUserForSession(accessor);
        
        if (userPrincipal == null) {
            throw new AccessDeniedException("Không thể xác định người dùng hiện tại");
        }

        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/chatrooms/")) {
            throw new AccessDeniedException("Destination không hợp lệ");
        }

        String chatRoomId = destination.substring("/topic/chatrooms/".length());
        String username = userPrincipal.getName();
        
        log.debug("Validating subscription for user: {} to chat room: {}", username, chatRoomId);

        // TODO: Implement chat room participant validation logic
        // For now, allow all authenticated users
        log.info("Subscription validated for user: {} to chat room: {}", username, chatRoomId);
    }

    private String resolveAuthorizationHeader(StompHeaderAccessor accessor) {
        // Try native headers first
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            return authHeaders.get(0);
        }
        
        // Try first-class headers
        Object authHeader = accessor.getHeader("Authorization");
        if (authHeader instanceof String) {
            return (String) authHeader;
        }
        
        return null;
    }

    private Message<?> createErrorMessage(StompHeaderAccessor accessor, String errorMessage) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setMessage(errorMessage);
        errorAccessor.setDestination(accessor.getDestination());
        
        if (accessor.getSessionId() != null) {
            errorAccessor.setSessionId(accessor.getSessionId());
        }
        
        return MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders());
    }
}