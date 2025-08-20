package iuh.house_keeping_service_be.config;

import iuh.house_keeping_service_be.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/v1/auth/login"
                                , "/api/v1/auth/logout"
                                , "/api/v1/auth/refresh-token"
                                , "/api/v1/auth/change-password"
                                , "/api/v1/auth/get-role").permitAll()

                        // Register endpoints - only EMPLOYEE and CUSTOMER can register
                        .requestMatchers("/api/v1/auth/register").hasAnyRole("EMPLOYEE", "CUSTOMER")

                        // Admin endpoints - only ADMIN role
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Employee endpoints - ADMIN and EMPLOYEE roles
                        .requestMatchers("/api/v1/employee/**").hasAnyRole("ADMIN", "EMPLOYEE")

                        // Customer endpoints - all authenticated users
                        .requestMatchers("/api/v1/customer/**").hasAnyRole("ADMIN", "EMPLOYEE", "CUSTOMER")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}