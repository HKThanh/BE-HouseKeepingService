package iuh.house_keeping_service_be.services.AuthService.impl;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.models.User;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.AuthService.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public String login(String username, String password, String role) {
        log.info("Login attempt for username: {}, role: {}", username, role);

        try {
            // Input validation
            if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                role == null || role.trim().isEmpty()) {
                throw new IllegalArgumentException("Username, password, and role are required");
            }

            // Authenticate user credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username.trim(), password)
            );

            // Find account and validate role
            Account account = accountRepository.findByUsername(username.trim())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if account is active
            if (!"ACTIVE".equals(account.getStatus())) {
                log.warn("Login attempt with inactive account: {}", username);
                throw new RuntimeException("Account is not active");
            }

            // Validate role matches account role
            Role requestedRole;
            try {
                requestedRole = Role.valueOf(role.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role provided: " + role);
            }

            if (!account.getRoles().equals(requestedRole)) {
                log.warn("Role mismatch for user {}: requested={}, actual={}",
                        username, requestedRole, account.getRoles());
                throw new RuntimeException("Role mismatch");
            }

            // Generate token with role information
            String token = jwtUtil.generateToken(username.trim());

            // Store token in Redis with user info
            redisTemplate.opsForValue().set(
                "token:" + token,
                username.trim() + ":" + account.getRoles().name(),
                jwtUtil.getExpiration() / 1000,
                TimeUnit.SECONDS
            );

            log.info("Successful login for user: {}", username);
            return token;

        } catch (Exception e) {
            log.error("Login failed for username: {}, error: {}", username, e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @Override
    public Account register(String username, String password, String email, String role, String fullName) {
        log.info("Registration attempt for username: {}, email: {}, role: {}", username, email, role);

        try {
            // Input validation
            validateRegistrationInput(username, password, email, role, fullName);

            // Check if username already exists
            if (accountRepository.findByUsername(username.trim()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }

            // Validate role
            Role accountRole;
            try {
                accountRole = Role.valueOf(role.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }

            // Hash the password
            String hashedPassword = passwordEncoder.encode(password);

            // Create specific user type based on role
            User user = createUserByRole(accountRole, email.trim(), fullName.trim());

            // Create Account
            Account account = new Account(username.trim(), hashedPassword, accountRole, "ACTIVE");
            account.setUser(user);

            // Save account
            Account savedAccount = accountRepository.save(account);
            log.info("Successfully registered user: {}", username);

            return savedAccount;

        } catch (Exception e) {
            log.error("Registration failed for username: {}, error: {}", username, e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            // Check if token exists in Redis
            String tokenKey = "token:" + token.trim();
            String userInfo = redisTemplate.opsForValue().get(tokenKey);

            if (userInfo == null) {
                log.debug("Token not found in Redis: {}", token);
                return false;
            }

            // Extract username from token
            String username = jwtUtil.extractUsername(token.trim());

            // Validate token using JWT utility
            boolean isValid = jwtUtil.validateToken(token.trim(), username);

            if (!isValid) {
                log.debug("Invalid JWT token for user: {}", username);
                redisTemplate.delete(tokenKey);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String refreshToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            // Validate current token first
            if (!validateToken(token.trim())) {
                throw new RuntimeException("Invalid token for refresh");
            }

            // Extract username from current token
            String username = jwtUtil.extractUsername(token.trim());

            // Get user info from Redis
            String tokenKey = "token:" + token.trim();
            String userInfo = redisTemplate.opsForValue().get(tokenKey);

            if (userInfo == null) {
                throw new RuntimeException("Token not found in Redis");
            }

            // Extract role from stored user info
            String[] parts = userInfo.split(":");
            if (parts.length != 2) {
                throw new RuntimeException("Invalid token data format");
            }

            String role = parts[1];

            // Generate new token
            String newToken = jwtUtil.generateToken(username);

            // Remove old token from Redis
            redisTemplate.delete(tokenKey);

            // Store new token in Redis
            redisTemplate.opsForValue().set(
                "token:" + newToken,
                username + ":" + role,
                jwtUtil.getExpiration() / 1000,
                TimeUnit.SECONDS
            );

            log.info("Token refreshed for user: {}", username);
            return newToken;

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }

    @Override
    public String logout(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            // Validate token before logout
            if (!validateToken(token.trim())) {
                throw new RuntimeException("Invalid token for logout");
            }

            // Remove token from Redis
            String tokenKey = "token:" + token.trim();
            redisTemplate.delete(tokenKey);

            log.info("User logged out successfully, token: {}", token);
            return "Logout successful";

        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }

    // Helper methods
    private void validateRegistrationInput(String username, String password, String email, String role, String fullName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }

    private User createUserByRole(Role accountRole, String email, String fullName) {
        User user;
        if (accountRole == Role.CUSTOMER) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFullName(fullName);
            user = customer;
        } else if (accountRole == Role.EMPLOYEE) {
            Employee employee = new Employee();
            employee.setEmail(email);
            employee.setFullName(fullName);
            user = employee;
        } else {
            user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
        }
        return user;
    }
}
