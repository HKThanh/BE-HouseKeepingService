package iuh.house_keeping_service_be.config.websocket;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket handshake rejected - missing token");
            return false;
        }

        try {
            String username = jwtUtil.extractUsername(token);
            if (!jwtUtil.validateToken(token, username)) {
                log.warn("WebSocket handshake rejected - invalid token for user {}", username);
                return false;
            }

            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

            attributes.put("accountId", account.getAccountId());
            attributes.put("username", account.getUsername());
            Set<String> roles = account.getRoles().stream()
                    .map(role -> role.getRoleName().name())
                    .collect(Collectors.toSet());
            attributes.put("roles", roles);

            return true;
        } catch (Exception ex) {
            log.warn("WebSocket handshake rejected - {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        // No-op
    }

    private String resolveToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            String header = httpServletRequest.getHeader("Authorization");
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                return header.substring(7);
            }

            String tokenParam = httpServletRequest.getParameter("token");
            if (!StringUtils.hasText(tokenParam)) {
                tokenParam = httpServletRequest.getParameter("access_token");
            }
            if (StringUtils.hasText(tokenParam)) {
                return tokenParam;
            }

        }
        return null;
    }
}