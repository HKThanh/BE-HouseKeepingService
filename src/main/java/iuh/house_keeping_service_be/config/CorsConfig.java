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
            "http://localhost:*",    // All localhost ports
            "http://127.0.0.1:*"     // All 127.0.0.1 ports
        ));

        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React dev server
            "http://localhost:5173",  // Vite dev server
            "http://localhost:8080",  // Local development
            "http://127.0.0.1:5500",  // Live Server
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        ));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}