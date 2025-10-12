package iuh.house_keeping_service_be.config;

import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.ConversationService.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AccountRepository accountRepository;
    private final ConversationService conversationService;

    @Override
    public Message<?> preSend(Message<?> message, org.springframework.messaging.MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (!StringUtils.hasText(token)) {
            throw new MessagingException("Thiếu thông tin xác thực");
        }
        String username = jwtUtil.extractUsername(token);
        if (!jwtUtil.validateToken(token, username)) {
            throw new MessagingException("Token không hợp lệ");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        log.info("Authenticated STOMP session for user {}", username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        accessor.setUser(authentication);
        log.debug("STOMP session {} is now authenticated", accessor.getSessionId());
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Authentication authentication = (Authentication) accessor.getUser();
        if (authentication == null) {
            throw new MessagingException("Người dùng chưa được xác thực");
        }
        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination) || !destination.startsWith("/topic/conversations/")) {
            return;
        }
        String conversationId = destination.substring("/topic/conversations/".length());
        String username = resolveUsername(authentication);
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new MessagingException("Tài khoản không hợp lệ"));
        if (!conversationService.isParticipant(conversationId, account.getAccountId())) {
            throw new MessagingException("Không có quyền truy cập cuộc hội thoại này");
        }
        log.info("User {} subscribed to conversation {}", username, conversationId);
        log.debug("Subscription headers: {}", accessor.toNativeHeaderMap());
    }

    private String resolveUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(authHeader)) {
            authHeader = accessor.getFirstNativeHeader("authorization");
        }
        if (!StringUtils.hasText(authHeader)) {
            authHeader = accessor.getFirstNativeHeader("token");
        }
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }
        authHeader = authHeader.trim();
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}