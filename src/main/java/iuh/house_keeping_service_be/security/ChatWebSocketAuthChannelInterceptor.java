package iuh.house_keeping_service_be.security;

import iuh.house_keeping_service_be.config.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
public class ChatWebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public ChatWebSocketAuthChannelInterceptor(JwtUtil jwtUtil,
                                               CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            StompCommand command = accessor.getCommand();

            if (StompCommand.CONNECT.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
                String authHeader = resolveAuthorizationHeader(accessor);

                if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        String username = jwtUtil.extractUsername(token);
                        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                        if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null,
                                            userDetails.getAuthorities());
                            accessor.setUser(authenticationToken);
                        } else {
                            log.warn("Invalid JWT token during WebSocket authentication for user: {}", username);
                            throw new IllegalArgumentException("Invalid JWT token");
                        }
                    } catch (Exception ex) {
                        log.error("Failed to authenticate WebSocket user: {}", ex.getMessage());
                        throw ex;
                    }
                }
            }
        }

        return message;
    }

    private String resolveAuthorizationHeader(StompHeaderAccessor accessor) {
        List<String> authorization = accessor.getNativeHeader("Authorization");
        if (!CollectionUtils.isEmpty(authorization)) {
            return authorization.get(0);
        }
        return accessor.getFirstNativeHeader("Authorization");
    }
}