package iuh.house_keeping_service_be.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS Configuration
 * 
 * NOTE: When deployed behind Nginx reverse proxy:
 * - REST API CORS: Handled by Nginx (prevents duplicate headers)
 * - WebSocket CORS: Handled by WebSocketConfig.registerStompEndpoints()
 * 
 * This class is kept for documentation purposes.
 * For local development without Nginx, uncomment the bean below.
 */
@Configuration
public class CorsConfig {

    // DISABLED: CORS is handled by:
    // 1. Nginx for REST API endpoints
    // 2. WebSocketConfig for WebSocket endpoints
    
    // Uncomment ONLY for local development without Nginx
    /*
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Use allowedOriginPatterns instead of allowedOrigins when credentials are true
        // configuration.setAllowedOriginPatterns(Arrays.asList(
        //     "http://localhost:*",    // All localhost ports
        //     "http://127.0.0.1:*"     // All 127.0.0.1 ports
        // ));

        // configuration.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:3000",  // React dev server
        //     "http://localhost:5173",  // Vite dev server
        //     "http://localhost:8080",  // Local development
        //     "http://127.0.0.1:5500",  // Live Server
        //     "http://127.0.0.1:3000",
        //     "http://127.0.0.1:5173"
        // ));

        // Use allowedOriginPatterns when credentials are needed
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Expose headers that client can read
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    */
}