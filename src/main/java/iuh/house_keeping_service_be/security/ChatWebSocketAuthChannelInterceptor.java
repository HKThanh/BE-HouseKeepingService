package iuh.house_keeping_service_be.security;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.ChatRoom;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.ChatRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ChatWebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final AccountRepository accountRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ChatWebSocketAuthChannelInterceptor(JwtUtil jwtUtil,
                                               CustomUserDetailsService customUserDetailsService,
                                               AccountRepository accountRepository,
                                               ChatRoomRepository chatRoomRepository) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.accountRepository = accountRepository;
        this.chatRoomRepository = chatRoomRepository;
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

            if (StompCommand.SUBSCRIBE.equals(command)) {
                validateSubscription(accessor);
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

    private void validateSubscription(StompHeaderAccessor accessor) {
        String chatRoomId = extractChatRoomId(accessor.getDestination());

        if (!StringUtils.hasText(chatRoomId)) {
            return;
        }

        Principal principal = accessor.getUser();
        String username = resolveUsername(principal);

        if (!StringUtils.hasText(username)) {
            log.warn("Subscription denied for chat room {} due to missing user information", chatRoomId);
            throw new AccessDeniedException("Không thể xác định người dùng hiện tại");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Subscription denied for chat room {} because account for username {} not found", chatRoomId, username);
                    return new AccessDeniedException("Tài khoản không tồn tại");
                });

        String accountId = account.getAccountId();

        if (!StringUtils.hasText(accountId)) {
            log.warn("Subscription denied for chat room {} because account {} lacks an ID", chatRoomId, username);
            throw new AccessDeniedException("Tài khoản không hợp lệ");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.warn("Subscription denied because chat room {} not found", chatRoomId);
                    return new AccessDeniedException("Không tìm thấy phòng chat");
                });

        boolean isParticipant = Optional.ofNullable(chatRoom.getCustomerAccount())
                .map(Account::getAccountId)
                .filter(accountId::equals)
                .isPresent()
                || Optional.ofNullable(chatRoom.getEmployeeAccount())
                .map(Account::getAccountId)
                .filter(accountId::equals)
                .isPresent();

        if (!isParticipant) {
            log.warn("Subscription denied for chat room {} because account {} is not a participant", chatRoomId, accountId);
            throw new AccessDeniedException("Bạn không có quyền truy cập phòng chat này");
        }
    }

    private String extractChatRoomId(String destination) {
        if (!StringUtils.hasText(destination)) {
            return null;
        }

        String prefix = "/topic/chatrooms/";

        if (!destination.startsWith(prefix)) {
            return null;
        }

        String path = destination.substring(prefix.length());
        int nextSeparator = path.indexOf('/');
        if (nextSeparator >= 0) {
            return path.substring(0, nextSeparator);
        }
        return path;
    }

    private String resolveUsername(Principal principal) {
        if (principal == null) {
            return null;
        }

        if (principal instanceof UsernamePasswordAuthenticationToken authenticationToken) {
            Object principalObject = authenticationToken.getPrincipal();
            if (principalObject instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }
            if (principalObject instanceof Principal nestedPrincipal) {
                return nestedPrincipal.getName();
            }
            if (principalObject instanceof String principalString) {
                return principalString;
            }
            return authenticationToken.getName();
        }

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return principal.getName();
    }
}