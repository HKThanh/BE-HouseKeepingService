package iuh.house_keeping_service_be.websocket.voice;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.repositories.VoiceBookingRequestRepository;
import iuh.house_keeping_service_be.services.VoiceBookingService.VoiceBookingEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;

/**
 * Applies JWT validation on CONNECT frames and enforces ownership on topic subscriptions.
 */
@Component
@Slf4j
public class VoiceBookingChannelInterceptor implements ChannelInterceptor {

    private static final String VOICE_TOPIC_PREFIX = "/topic/voice-booking/";

    private final JwtUtil jwtUtil;
    private final VoiceBookingRequestRepository voiceBookingRequestRepository;
    private final VoiceBookingEventPublisher eventPublisher;

    public VoiceBookingChannelInterceptor(
            JwtUtil jwtUtil,
            VoiceBookingRequestRepository voiceBookingRequestRepository,
            @Lazy VoiceBookingEventPublisher eventPublisher
    ) {
        this.jwtUtil = jwtUtil;
        this.voiceBookingRequestRepository = voiceBookingRequestRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        Boolean voiceSession = (Boolean) accessor.getSessionAttributes().get("voiceBookingSession");
        if (!Boolean.TRUE.equals(voiceSession)) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            handleSubscribe(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader)) {
            authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION.toLowerCase());
        }

        if (!StringUtils.hasText(authHeader)) {
            Principal user = accessor.getUser();
            if (user == null) {
                throw new MessagingException("Authorization header is required for voice booking channel");
            }
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new MessagingException("Authorization header must contain Bearer token");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (!jwtUtil.validateToken(token, username)) {
            throw new MessagingException("JWT token expired or invalid");
        }

        String role = normalizeRole(jwtUtil.extractRole(token));
        if (!"ROLE_CUSTOMER".equals(role)) {
            throw new MessagingException("Voice booking WebSocket is restricted to ROLE_CUSTOMER");
        }

        VoiceBookingPrincipal principal = new VoiceBookingPrincipal(username);
        accessor.setUser(principal);
        accessor.getSessionAttributes().put("voiceBookingUser", username);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();

        if (!StringUtils.hasText(destination) || user == null) {
            throw new MessagingException("Missing subscription destination or principal");
        }

        if (destination.startsWith(VOICE_TOPIC_PREFIX)) {
            String requestId = destination.substring(VOICE_TOPIC_PREFIX.length());
            boolean ownsRequest = voiceBookingRequestRepository
                    .existsByIdAndCustomer_Account_Username(requestId, user.getName());

            if (!ownsRequest) {
                log.warn("User {} attempted to subscribe to unauthorized voice booking {}", user.getName(), requestId);
                eventPublisher.publishConnectionError(
                        user.getName(),
                        requestId,
                        "Bạn không có quyền theo dõi trạng thái voice booking này.",
                        "VOICE_BOOKING_FORBIDDEN"
                );
                throw new MessagingException("Unauthorized voice booking subscription");
            }
        } else if (destination.endsWith("voice-booking/errors")) {
            // Allow user-specific error queue subscriptions
            log.debug("User {} subscribed to {}", user.getName(), destination);
        }
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "";
        }
        String normalized = role.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return normalized;
    }
}
