package iuh.house_keeping_service_be.websocket.voice;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.http.server.ServerHttpRequest;

import java.security.Principal;
import java.util.Map;

/**
 * Injects a Principal into the WebSocket session so user-specific destinations work.
 */
@Component
public class VoiceBookingHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Object principal = attributes.get("voiceBookingPrincipal");
        if (principal instanceof Principal p) {
            return p;
        }

        String username = (String) attributes.get("voiceBookingUser");
        if (StringUtils.hasText(username)) {
            return new VoiceBookingPrincipal(username);
        }

        return super.determineUser(request, wsHandler, attributes);
    }
}
