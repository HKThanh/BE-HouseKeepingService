package iuh.house_keeping_service_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * TaskScheduler for WebSocket heartbeat
     * This prevents connection from being closed due to inactivity
     */
    @Bean
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker with heartbeat configuration
        // This prevents automatic disconnect after sending messages or during idle time
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000}) // 10s heartbeat (send, receive)
                .setTaskScheduler(heartBeatScheduler());
        
        // Prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the WebSocket endpoint with CORS configuration - Allow all origins
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000); // 25s SockJS heartbeat
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Configure WebSocket transport settings to prevent early disconnection
        registration
                .setMessageSizeLimit(128 * 1024)        // 128 KB max message size
                .setSendBufferSizeLimit(512 * 1024)     // 512 KB send buffer
                .setSendTimeLimit(20 * 1000)            // 20s send timeout
                .setTimeToFirstMessage(30 * 1000);      // 30s time to first message
    }
}
