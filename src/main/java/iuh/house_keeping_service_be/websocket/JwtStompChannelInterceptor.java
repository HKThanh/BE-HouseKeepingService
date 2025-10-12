package iuh.house_keeping_service_be.websocket;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            log.info("Session {} subscribed to {}", accessor.getSessionId(), accessor.getDestination());
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            log.info("Session {} disconnected", accessor.getSessionId());
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        try {
            String username = jwtUtil.extractUsername(token);
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new AccessDeniedException("Không tìm thấy tài khoản"));

            UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(
                    account.getAccountId(),
                    null,
                    Collections.emptyList()
            );
            accessor.setUser(user);
            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put("accountId", account.getAccountId());
                accessor.getSessionAttributes().put("username", account.getUsername());
            }
            log.info("WebSocket CONNECT accepted for account {} on session {}", account.getAccountId(), accessor.getSessionId());
        } catch (Exception ex) {
            log.error("WebSocket authentication failed: {}", ex.getMessage());
            throw new AccessDeniedException("Xác thực WebSocket thất bại: " + ex.getMessage());
        }
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authHeader)) {
            String token = extractToken(authHeader);
            if (StringUtils.hasText(token)) {
                return token;
            }
        }

        if (accessor.getSessionAttributes() != null) {
            Object token = accessor.getSessionAttributes().get("token");
            if (token instanceof String str && StringUtils.hasText(str)) {
                return str;
            }
        }

        Principal user = accessor.getUser();
        if (user != null && StringUtils.hasText(user.getName())) {
            return user.getName();
        }

        throw new AccessDeniedException("Thiếu token xác thực");
    }

    private String extractToken(String headerValue) {
        String value = headerValue.trim();
        if (!StringUtils.hasText(value)) {
            return null;
        }

        if (value.regionMatches(true, 0, "Bearer", 0, "Bearer".length())) {
            String remainder = value.substring("Bearer".length()).trim();
            return StringUtils.hasText(remainder) ? remainder : null;
        }

        return value;
    }
}