package iuh.house_keeping_service_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Use allowedOriginPatterns instead of allowedOrigins when credentials are true
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "https://web.postman.co",
                "https://app.getpostman.com",
                "https://*.postman.com",
                "https://websocketking.com",
                "*"
        ));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Cache-Control", 
            "Content-Type",
            "Sec-WebSocket-Accept",
            "Sec-WebSocket-Extensions",
            "Sec-WebSocket-Key",
            "Sec-WebSocket-Protocol",
            "Sec-WebSocket-Version"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}