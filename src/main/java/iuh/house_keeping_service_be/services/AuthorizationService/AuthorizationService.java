package iuh.house_keeping_service_be.services.AuthorizationService;

import iuh.house_keeping_service_be.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    @Autowired
    private JwtUtil jwtUtil;

    public String getCurrentUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        return jwtUtil.extractUsername(token);
    }

    public String getCurrentUserRole(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        return jwtUtil.extractRole(token);
    }

    public boolean canAccessResource(String authHeader, String resourceId) {
        String currentUserId = getCurrentUserId(authHeader);
        String currentUserRole = getCurrentUserRole(authHeader);

        // Admin can access any resource
        if ("ADMIN".equals(currentUserRole)) {
            return true;
        }

        // Users can only access their own resources
        return currentUserId.equals(resourceId);
    }
}