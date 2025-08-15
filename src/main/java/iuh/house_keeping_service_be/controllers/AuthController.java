package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Authentication.*;
import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.AdminProfile;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.AdminService.AdminService;
import iuh.house_keeping_service_be.services.AuthService.AuthService;
import iuh.house_keeping_service_be.services.AuthService.impl.AuthServiceImpl;
import iuh.house_keeping_service_be.services.CustomerService.CustomerService;
import iuh.house_keeping_service_be.services.EmployeeService.EmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private static final int EXPIRATION_TIME = 3600; // 1 hour in seconds

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthServiceImpl authServiceImpl;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.username();
        String password = loginRequest.password();
        String requestedRole = loginRequest.role();

        log.info("Login attempt for username: {}, role: {}", username, requestedRole);

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tên đăng nhập không được để trống"
            ));
        }

        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Mật khẩu không được để trống"
            ));
        }

        try {
            // Check if account is locked
            String lockKey = "login:locked:" + username;
            Boolean isLocked = redisTemplate.hasKey(lockKey);
            if (Boolean.TRUE.equals(isLocked)) {
                log.warn("Login attempt on locked account: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Tài khoản tạm thời bị khóa do đăng nhập sai nhiều lần"
                ));
            }

            try {
                // Get token pair first without modifying any database records
                TokenPair tokenPair = authService.login(username, loginRequest.password(), requestedRole);

                // Find account for role info after successful authentication
                Account account = accountRepository.findByUsername(username.trim())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Role role = account.getRole();

                // Store the token in Redis (use String values instead of objects to avoid serialization issues)
                String userSessionKey = "user_session:" + username;
                // Clear any existing session
                Object existingToken = redisTemplate.opsForValue().get(userSessionKey);
                if (existingToken != null) {
                    redisTemplate.delete("access_token:" + existingToken.toString());
                    redisTemplate.delete("refresh_token:" + existingToken.toString());
                }

                // Store new session
                redisTemplate.opsForValue().set(
                        userSessionKey,
                        tokenPair.accessToken(),
                        jwtUtil.getAccessExpiration() / 1000,
                        TimeUnit.SECONDS
                );

                // Fetch user profile data based on role
                DataLoginResponse data = null;
                AdminLoginResponse dataAdmin = null;
                switch (role) {
                    case ADMIN:
                        AdminProfile adminProfile = adminService.findByAccountId(account.getAccountId());

                        dataAdmin = new AdminLoginResponse(
                                account.getUsername(),
                                adminProfile.getFullName(),
                                adminProfile.getIsMale(),
                                adminProfile.getAddress(),
                                adminProfile.getDepartment(),
                                adminProfile.getContactInfo(),
                                adminProfile.getHireDate().toString()
                        );
                        break;

                    case CUSTOMER:
                        Customer customer = customerService.findByAccountId(account.getAccountId());
                        data = new DataLoginResponse(
                                account.getUsername(),
                                customer.getAvatar(),
                                customer.getFullName(),
                                customer.getEmail(),
                                customer.getPhoneNumber(),
                                customer.getIsMale(),
                                account.getStatus().name(),
                                customer.getAddress()
                        );
                        break;

                    case EMPLOYEE:
                        Employee employee = employeeService.findByAccountId(account.getAccountId());
                        data = new DataLoginResponse(
                                account.getUsername(),
                                employee.getAvatar(),
                                employee.getFullName(),
                                employee.getEmail(),
                                employee.getPhoneNumber(),
                                employee.getIsMale(),
                                account.getStatus().name(),
                                employee.getAddress()
                        );
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid role: " + role);
                }

                // Update last login time in a separate transaction
                try {
                    authServiceImpl.updateLastLoginTime(account);
                } catch (Exception e) {
                    // Log but don't fail the login if updating last login fails
                    log.warn("Failed to update last login time: {}", e.getMessage());
                }

                if (account.getRole() == Role.ADMIN) {
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Đăng nhập thành công",
                            "data", Map.of(
                                    "accessToken", tokenPair.accessToken(),
                                    "refreshToken", tokenPair.refreshToken(),
                                    "expireIn", EXPIRATION_TIME,
                                    "role", role.name(),
                                    "data", dataAdmin
                            )
                    ));
                }

                // Create response with tokens and user data
                LoginResponse response = new LoginResponse(
                        tokenPair.accessToken(),
                        tokenPair.refreshToken(),
                        EXPIRATION_TIME,
                        role.name(),
                        data
                );

                // Reset failed login attempts on successful login
                redisTemplate.delete("login:failed:" + username);

                log.info("Login successful for user: {}, role: {}", username, role);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đăng nhập thành công",
                        "data", response
                ));

            } catch (Exception e) {
                incrementFailedLoginAttempts(username);

                log.warn("Authentication failed for user: {}, error: {}", username, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Thông tin đăng nhập không hợp lệ"
                ));
            }

        } catch (Exception e) {
            log.error("Login failed for user: {}, error: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi đăng nhập"
            ));
        }
    }
    /**
     * Tracks failed login attempts and locks accounts after multiple failures
     */
    private void incrementFailedLoginAttempts(String username) {
        String failedKey = "login:failed:" + username;
        String lockKey = "login:locked:" + username;

        Long attempts = redisTemplate.opsForValue().increment(failedKey);

        // Lock account after 3 failed attempts
        if (attempts >= 3) {
            // Lock for 10 minutes
            redisTemplate.opsForValue().set(lockKey, "locked", 600, TimeUnit.SECONDS);
            redisTemplate.delete(failedKey);
            log.warn("Account locked due to multiple failed login attempts: {}", username);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            Account account = authService.register(
                    registerRequest.username(),
                    registerRequest.password(),
                    registerRequest.email(),
                    registerRequest.role(),
                    registerRequest.fullName()
            );

            RegisterResponse response = new RegisterResponse(
                    account.getUsername(),
                    registerRequest.email(),
                    registerRequest.fullName()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Registration successful",
                    "data", response
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Registration failed"
            ));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Authorization header is required"
                ));
            }

            TokenPair tokenPair = authService.refreshToken(request.refreshToken());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token refreshed successfully",
                    "data", new TokenRefreshResponse(
                            tokenPair.accessToken(),
                            tokenPair.refreshToken()
                    )
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Token refresh failed"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Authorization header is required"
                ));
            }

            String token = authHeader.substring(7);
            authService.logout(token);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful"
            ));

        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Logout failed"
            ));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Authorization header is required"
                ));
            }

            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token is valid",
                    "valid", true
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token is invalid",
                    "valid", false
                ));
            }

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Token validation failed",
                "valid", false
            ));
        }
    }
}