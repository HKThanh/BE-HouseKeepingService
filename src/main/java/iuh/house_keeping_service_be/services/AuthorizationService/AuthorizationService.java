package iuh.house_keeping_service_be.services.AuthorizationService;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.repositories.AdminProfileRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final AdminProfileRepository adminProfileRepository;

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

        log.info("Current User ID: {}, Role: {}, Resource ID: {}", currentUserId, currentUserRole, resourceId);

        // Admin can access any resource (handle both formats)
        if ("ADMIN".equals(currentUserRole) || "ROLE_ADMIN".equals(currentUserRole)) {
            return true;
        }

        switch (currentUserRole) {
            case "CUSTOMER", "ROLE_CUSTOMER" -> {
                // Customer can only access their own resource
                return currentUserId.equals(resourceId) ||
                       customerRepository.findByAccount_Username(currentUserId)
                                         .map(c -> c.getCustomerId().equals(resourceId))
                                         .orElse(false);
            }
            case "EMPLOYEE", "ROLE_EMPLOYEE" -> {
                // Employee can only access their own resource
                return currentUserId.equals(resourceId) ||
                       employeeRepository.findByAccount_Username(currentUserId)
                                         .map(e -> e.getEmployeeId().equals(resourceId))
                                         .orElse(false);
            }
            case "ADMIN_PROFILE", "ROLE_ADMIN_PROFILE" -> {
                // Admin Profile can only access their own resource
                return currentUserId.equals(resourceId) ||
                       adminProfileRepository.findByAccount_Username(currentUserId)
                                             .map(a -> a.getAdminProfileId().equals(resourceId))
                                             .orElse(false);
            }
            default -> {
                log.warn("Unknown role: {}", currentUserRole);
                return false;
            }
        }
    }
}