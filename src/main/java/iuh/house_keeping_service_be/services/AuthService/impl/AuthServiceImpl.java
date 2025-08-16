package iuh.house_keeping_service_be.services.AuthService.impl;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Authentication.TokenPair;
import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.AdminProfile;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.AdminProfileRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.services.AuthService.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
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
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AdminProfileRepository adminProfileRepository;

    @Override
    public TokenPair login(String username, String password, String role, String deviceType) {
        log.info("Login attempt for username: {}, role: {}, device: {}", username, role, deviceType);
    
        try {
            // Input validation
            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    role == null || role.trim().isEmpty() ||
                    deviceType == null || deviceType.trim().isEmpty()) {
                throw new IllegalArgumentException("Username, password, role, and device type are required");
            }
    
            // Authenticate user credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username.trim(), password)
            );
    
            // Find account and validate role
            Account account = accountRepository.findByUsername(username.trim())
                    .orElseThrow(() -> new RuntimeException("User not found"));
    
            // Explicitly verify password
            if (!passwordEncoder.matches(password, account.getPassword())) {
                log.warn("Invalid password for user: {}", username);
                throw new RuntimeException("Invalid credentials");
            }
    
            // Validate role matches account role
            Role requestedRole;
            try {
                requestedRole = Role.valueOf(role.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role provided: " + role);
            }
    
            if (!account.getRole().equals(requestedRole)) {
                log.warn("Role mismatch for user {}: requested={}, actual={}",
                        username, requestedRole, account.getRole());
                throw new RuntimeException("Role mismatch");
            }
    
            // Generate token pair
            TokenPair tokenPair = jwtUtil.generateTokenPair(username.trim());
    
            // Store access token in Redis with device-specific key
            redisTemplate.opsForValue().set(
                    "access_token:" + tokenPair.accessToken(),
                    username.trim() + ":" + account.getRole() + ":" + deviceType,
                    jwtUtil.getAccessExpiration() / 1000,
                    TimeUnit.SECONDS
            );
    
            // Store refresh token in Redis with device-specific key
            redisTemplate.opsForValue().set(
                    "refresh_token:" + tokenPair.refreshToken(),
                    username.trim() + ":" + account.getRole() + ":" + deviceType,
                    jwtUtil.getRefreshExpiration() / 1000,
                    TimeUnit.SECONDS
            );
    
            return tokenPair;
    
        } catch (Exception e) {
            log.error("Login failed for username: {}, error: {}", username, e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @Override
    public Account register(String username, String password, String email, String role, String fullName, String phoneNumber) {
        log.info("Registration attempt for username: {}, email: {}, role: {}", username, email, role);

        try {
            // Input validation
            validateRegistrationInput(username, password, email, role, fullName);

            // Check if username already exists
            if (accountRepository.findByUsername(username.trim()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }

            // Validate and convert role string to enum
            Role accountRole;
            try {
                accountRole = Role.valueOf(role.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role + ". Must be CUSTOMER, EMPLOYEE, or ADMIN");
            }

            // Generate UUID for account
            String accountId = UUID.randomUUID().toString();

            // Create account with basic information
            Account account = new Account();
            account.setAccountId(accountId);
            account.setUsername(username.trim());
            account.setPassword(passwordEncoder.encode(password));
            account.setRole(accountRole);
            account.setStatus(AccountStatus.ACTIVE);
            account.setCreatedAt(Instant.now());
            account.setUpdatedAt(Instant.now());

            // Create specific user type based on role
            switch (accountRole) {
                case CUSTOMER -> {
                    Customer customer = new Customer();
                    customer.setCustomerId(UUID.randomUUID().toString());
                    customer.setAccount(account);
                    customer.setFullName(fullName.trim());
                    customer.setEmail(email.trim());
                    customer.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);
                    customer.setCreatedAt(Instant.now());
                    customer.setUpdatedAt(Instant.now());
                    customerRepository.save(customer);
                }
                case EMPLOYEE -> {
                    Employee employee = new Employee();
                    employee.setEmployeeId(UUID.randomUUID().toString());
                    employee.setAccount(account);
                    employee.setFullName(fullName.trim());
                    employee.setEmail(email.trim());
                    employee.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);
                    employee.setHiredDate(LocalDate.now());
                    employee.setCreatedAt(Instant.now());
                    employee.setUpdatedAt(Instant.now());
                    employeeRepository.save(employee);
                }
                case ADMIN -> {
                    AdminProfile admin = new AdminProfile();
                    admin.setAdminProfileId(UUID.randomUUID().toString());
                    admin.setAccount(account);
                    admin.setContactInfo(email.trim());
                    admin.setHireDate(LocalDate.now());
                    admin.setCreatedAt(Instant.now());
                    admin.setUpdatedAt(Instant.now());
                    adminProfileRepository.save(admin);
                }
                default -> throw new IllegalArgumentException("Unsupported role: " + accountRole);
            }

            // Save account
            Account savedAccount = accountRepository.save(account);
            log.info("Successfully registered user: {}", username);

            return savedAccount;

        } catch (IllegalArgumentException e) {
            log.error("Registration validation error for username: {}, error: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Registration failed for username: {}, error: {}", username, e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
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
            Object userInfoObj = redisTemplate.opsForValue().get(tokenKey);

            // Check if token exists
            if (userInfoObj == null) {
                log.debug("Token not found in Redis: {}", token);
                return false;
            }

            String userInfo = userInfoObj.toString();

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
    public TokenPair refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        // Validate refresh token
        String refreshTokenKey = "refresh_token:" + refreshToken.trim();
        Object userInfoObj = redisTemplate.opsForValue().get(refreshTokenKey);

        if (userInfoObj == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        String userInfo = userInfoObj.toString();

        String[] parts = userInfo.split(":");
        String username = parts[0];
        String role = parts[1];

        // Generate new token pair
        TokenPair newTokenPair = jwtUtil.generateTokenPair(username);

        // Delete old refresh token
        redisTemplate.delete(refreshTokenKey);

        // Store new tokens in Redis
        redisTemplate.opsForValue().set(
                "access_token:" + newTokenPair.accessToken(),
                username + ":" + role,
                jwtUtil.getAccessExpiration() / 1000,
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                "refresh_token:" + newTokenPair.refreshToken(),
                username + ":" + role,
                jwtUtil.getRefreshExpiration() / 1000,
                TimeUnit.SECONDS
        );

        return newTokenPair;
    }

    @Override
    public String logout(String token) {
        try {
            // Get token info
            String tokenKey = "access_token:" + token;
            Object tokenInfoObj = redisTemplate.opsForValue().get(tokenKey);

            if (tokenInfoObj != null) {
                String tokenInfo = tokenInfoObj.toString();
                String[] parts = tokenInfo.split(":");
                String username = parts[0];
                String deviceType = parts.length > 2 ? parts[2] : "UNKNOWN";

                // Remove user session for this device
                String userSessionKey = "user_session:" + username + ":" + deviceType;
                redisTemplate.delete(userSessionKey);

                // Find and remove corresponding refresh token
                Set<String> refreshTokenKeys = (Set<String>) (Set<?>) redisTemplate.keys("refresh_token:*");
                if (refreshTokenKeys != null) {
                    for (String refreshKey : refreshTokenKeys) {
                        Object refreshTokenInfo = redisTemplate.opsForValue().get(refreshKey);
                        if (refreshTokenInfo != null && refreshTokenInfo.toString().equals(tokenInfo)) {
                            redisTemplate.delete(refreshKey);
                            break;
                        }
                    }
                }
            }

            // Remove access token
            redisTemplate.delete(tokenKey);

            return "Logout successful";
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }

    @Override
    public String logoutAllDevices(String username) {
        try {
            // Get all user sessions
            Set<String> userSessions = (Set<String>) (Set<?>) redisTemplate.keys("user_session:" + username + ":*");
            if (userSessions != null) {
                for (String sessionKey : userSessions) {
                    Object sessionToken = redisTemplate.opsForValue().get(sessionKey);
                    if (sessionToken != null) {
                        // Delete corresponding access and refresh tokens
                        redisTemplate.delete("access_token:" + sessionToken.toString());

                        // Find and delete refresh token
                        Set<String> refreshTokenKeys = (Set<String>) (Set<?>) redisTemplate.keys("refresh_token:*");
                        if (refreshTokenKeys != null) {
                            for (String refreshKey : refreshTokenKeys) {
                                Object refreshTokenInfo = redisTemplate.opsForValue().get(refreshKey);
                                if (refreshTokenInfo != null && refreshTokenInfo.toString().startsWith(username + ":")) {
                                    redisTemplate.delete(refreshKey);
                                }
                            }
                        }
                    }
                    // Delete session key
                    redisTemplate.delete(sessionKey);
                }
            }

            return "Logged out from all devices successfully";
        } catch (Exception e) {
            log.error("Logout all devices error: {}", e.getMessage());
            throw new RuntimeException("Logout from all devices failed: " + e.getMessage());
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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastLoginTime(Account account) {
        // Fetch a fresh managed entity to avoid detached entity issues
        Account managedAccount = accountRepository.findById(account.getAccountId().toString())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        managedAccount.setLastLogin(Instant.now());
        accountRepository.saveAndFlush(managedAccount);
    }
}