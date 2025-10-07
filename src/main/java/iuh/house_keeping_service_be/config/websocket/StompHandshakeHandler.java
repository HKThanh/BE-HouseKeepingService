package iuh.house_keeping_service_be.config.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
public class StompHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request,
                                      @NonNull WebSocketHandler wsHandler,
                                      @NonNull Map<String, Object> attributes) {
        String accountId = (String) attributes.get("accountId");
        String username = (String) attributes.get("username");
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) attributes.getOrDefault("roles", Collections.emptySet());

        if (StringUtils.hasText(accountId) && StringUtils.hasText(username)) {
            return new StompPrincipal(accountId, username, roles);
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}