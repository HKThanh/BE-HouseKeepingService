package iuh.house_keeping_service_be.websocket.voice;

import iuh.house_keeping_service_be.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Validates JWT token during the WebSocket handshake for /ws/voice-booking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VoiceBookingHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Voice booking WebSocket handshake rejected: missing Authorization header");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(token);
            if (!jwtUtil.validateToken(token, username)) {
                log.warn("Voice booking WebSocket handshake rejected: invalid or expired token for user {}", username);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            String role = normalizeRole(jwtUtil.extractRole(token));
            if (!"ROLE_CUSTOMER".equals(role)) {
                log.warn("Voice booking WebSocket handshake rejected: user {} without ROLE_CUSTOMER", username);
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return false;
            }

            VoiceBookingPrincipal principal = new VoiceBookingPrincipal(username);
            attributes.put("voiceBookingPrincipal", principal);
            attributes.put("voiceBookingUser", username);
            attributes.put("voiceBookingRole", role);
            attributes.put("voiceBookingSession", Boolean.TRUE);

            return true;
        } catch (Exception exception) {
            log.warn("Voice booking WebSocket handshake rejected: {}", exception.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // No-op
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
