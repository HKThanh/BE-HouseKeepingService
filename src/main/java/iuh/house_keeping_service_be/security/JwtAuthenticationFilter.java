package iuh.house_keeping_service_be.security;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.services.AuthService.AuthService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        boolean isSessionsEndpoint = "/api/v1/auth/sessions".equals(requestURI);

        try {
            String authHeader = request.getHeader("Authorization");

            // Special handling for sessions endpoint - return 400 for missing header
            if (isSessionsEndpoint && (authHeader == null || !authHeader.startsWith("Bearer "))) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Authorization header is required\"}");
                return;
            }

            // Normal JWT processing for all endpoints with Authorization header
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validateToken(token, userDetails.getUsername()) &&
                            authService.validateToken(token)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }

        } catch (MalformedJwtException e) {
            // Special handling for sessions endpoint - return 400 for malformed JWT
            if (isSessionsEndpoint) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Authorization header is required\"}");
                return;
            }
            // For other endpoints, let Spring Security handle it (will return 403/401)
            log.error("Malformed JWT token for URI: {}, error: {}", requestURI, e.getMessage());
        } catch (Exception e) {
            log.error("JWT filter error for URI: {}, error: {}", requestURI, e.getMessage());
            // For non-sessions endpoints, let Spring Security handle authentication failures
            if (!isSessionsEndpoint) {
                // Continue with filter chain - Spring Security will handle the response
            }
        }

        filterChain.doFilter(request, response);
    }
}