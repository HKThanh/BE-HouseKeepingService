package iuh.house_keeping_service_be.config;

import iuh.house_keeping_service_be.security.ChatWebSocketAuthChannelInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private final ChatWebSocketAuthChannelInterceptor chatWebSocketAuthChannelInterceptor;

    public WebSocketConfig(ChatWebSocketAuthChannelInterceptor chatWebSocketAuthChannelInterceptor) {
        this.chatWebSocketAuthChannelInterceptor = chatWebSocketAuthChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic/chatrooms", "/queue/chatrooms");
        registry.setUserDestinationPrefix("/user");
    }

        @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, 
                                                    WebSocketHandler wsHandler, 
                                                    Map<String, Object> attributes) {
                        log.info("WebSocket handshake from: {}", request.getRemoteAddress());
                        
                        // Extract JWT token from Authorization header
                        String authHeader = request.getHeaders().getFirst("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7);
                            attributes.put("jwt_token", token);
                            log.debug("Stored JWT token in handshake attributes");
                        } else {
                            // Also check query parameters for token (for clients that can't set headers)
                            String tokenParam = request.getURI().getQuery();
                            if (tokenParam != null && tokenParam.contains("token=")) {
                                String token = tokenParam.split("token=")[1].split("&")[0];
                                attributes.put("jwt_token", token);
                                log.debug("Stored JWT token from query parameter");
                            }
                        }
                        
                        return super.determineUser(request, wsHandler, attributes);
                    }
                });
        
        // SockJS fallback với session configuration
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000)
                .setSessionCookieNeeded(false); // Không cần cookies cho JWT auth
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Logging interceptor trước
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    log.debug("=== WebSocket Message ===");
                    log.debug("Command: {}", accessor.getCommand());
                    log.debug("Session: {}", accessor.getSessionId());
                    log.debug("User: {}", accessor.getUser());
                    log.debug("Destination: {}", accessor.getDestination());
                    log.debug("Session Attributes: {}", accessor.getSessionAttributes().keySet());
                    log.debug("========================");
                }
                
                return message;
            }
        });
    
        // Auth interceptor sau
        registration.interceptors(chatWebSocketAuthChannelInterceptor);
    }
}