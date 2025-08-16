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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
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
        String deviceType = loginRequest.deviceType();
    
        log.info("Login attempt for username: {}, role: {}, device: {}", username, requestedRole, deviceType);
    
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

        if (requestedRole == null || requestedRole.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Vai trò không được để trống"
            ));
        }

        if (!requestedRole.matches("CUSTOMER|EMPLOYEE|ADMIN")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Vai trò không hợp lệ. Chỉ chấp nhận CUSTOMER, EMPLOYEE hoặc ADMIN"
            ));
        }

        if (deviceType == null || deviceType.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Loại thiết bị không được để trống"
            ));
        }

        if (!deviceType.matches("WEB|MOBILE")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Loại thiết bị không hợp lệ. Chỉ chấp nhận WEB hoặc MOBILE"
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
                TokenPair tokenPair = authService.login(username, loginRequest.password(), requestedRole, deviceType);
    
                // Find account for role info after successful authentication
                Account account = accountRepository.findByUsername(username.trim())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (account.getStatus() != AccountStatus.ACTIVE) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                            "success", false,
                            "message", "Tài khoản chưa được kích hoạt hoặc đã bị khóa"
                    ));
                }
    
                Role role = account.getRole();
    
                // Store the token in Redis with device-specific key
                String userSessionKey = "user_session:" + username + ":" + deviceType;
                
                // Get existing session for this device type
                Object existingToken = redisTemplate.opsForValue().get(userSessionKey);
                if (existingToken != null) {
                    // Clean up old tokens for this device
                    redisTemplate.delete("access_token:" + existingToken.toString());
                    redisTemplate.delete("refresh_token:" + existingToken.toString());
                }
    
                // Store new session for this device
                redisTemplate.opsForValue().set(
                        userSessionKey,
                        tokenPair.accessToken(),
                        jwtUtil.getAccessExpiration() / 1000,
                        TimeUnit.SECONDS
                );
    
                // Rest of the existing code for fetching user profile data...
                EmployeeLoginResponse dataEmployee = null;
                CustomerLoginResponse dataCustomer = null;
                DataLoginResponse data = null;
                AdminLoginResponse dataAdmin = null;
    
                switch (role) {
                    case ADMIN:
                        AdminProfile adminProfile = adminService.findByAccountId(account.getAccountId());
                        dataAdmin = new AdminLoginResponse(
                                adminProfile.getAdminProfileId(),
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
                        dataCustomer = new CustomerLoginResponse(
                                customer.getCustomerId(),
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
                        dataEmployee = new EmployeeLoginResponse(
                                employee.getEmployeeId(),
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
    
                // Reset failed login attempts on successful login
                redisTemplate.delete("login:failed:" + username);
    
                log.info("Login successful for user: {}, role: {}, device: {}", username, role, deviceType);
    
                // Update last login time
                try {
                    authServiceImpl.updateLastLoginTime(account);
                } catch (Exception e) {
                    log.warn("Failed to update last login time: {}", e.getMessage());
                }
    
                Map<String, Object> responseData = Map.of(
                        "accessToken", tokenPair.accessToken(),
                        "refreshToken", tokenPair.refreshToken(),
                        "expireIn", EXPIRATION_TIME,
                        "role", role.name(),
                        "deviceType", deviceType,
                        "data", role == Role.ADMIN ? dataAdmin : 
                                role == Role.EMPLOYEE ? dataEmployee : dataCustomer
                );
    
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đăng nhập thành công",
                        "data", responseData
                ));
    
            } catch (RuntimeException re) {
                log.error("Login error for user: {}, error: {}", username, re.getMessage(), re);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tài khoản hoặc mật khẩu không hợp lệ"
                ));
            }
            catch (Exception e) {
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

    @GetMapping("/sessions")
    public ResponseEntity<?> getActiveSessions(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Authorization header is required"
                ));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            Set<Object> webSessionsObj = redisTemplate.keys("user_session:" + username + ":WEB");
            Set<String> webSessions = webSessionsObj != null ?
                webSessionsObj.stream().map(Object::toString).collect(Collectors.toSet()) :
                Collections.emptySet();
            Set<Object> mobileSessionsObj = redisTemplate.keys("user_session:" + username + ":MOBILE");
            Set<String> mobileSessions = mobileSessionsObj != null ?
                mobileSessionsObj.stream().map(Object::toString).collect(Collectors.toSet()) :
                Collections.emptySet();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "webSessions", webSessions != null ? webSessions.size() : 0,
                            "mobileSessions", mobileSessions != null ? mobileSessions.size() : 0
                    )
            ));

        } catch (Exception e) {
            log.error("Error getting active sessions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get active sessions"
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            Account account = authService.register(
                    registerRequest.username(),
                    registerRequest.password(),
                    registerRequest.email(),
                    registerRequest.phoneNumber(),
                    registerRequest.role(),
                    registerRequest.fullName()
            );

            RegisterResponse response = new RegisterResponse(
                    account.getUsername(),
                    registerRequest.email(),
                    registerRequest.role()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Đăng ký thành công",
                    "data", response
            ));

        } catch (IllegalArgumentException e) {
            // Parse the error message to determine the field
            String field = extractFieldFromErrorMessage(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "field", field
            ));
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi đăng ký tài khoản"
            ));
        }
    }

    private String extractFieldFromErrorMessage(String message) {
        String lowerMessage = message.toLowerCase();

        // Check for Vietnamese terms
        if (lowerMessage.contains("tên đăng nhập") || lowerMessage.contains("username")) return "username";
        if (lowerMessage.contains("email")) return "email";
        if (lowerMessage.contains("số điện thoại") || lowerMessage.contains("phone")) return "phoneNumber";
        if (lowerMessage.contains("vai trò") || lowerMessage.contains("role")) return "role";
        if (lowerMessage.contains("mật khẩu") || lowerMessage.contains("password")) return "password";
        if (lowerMessage.contains("họ và tên") || lowerMessage.contains("full name")) return "fullName";

        return "general";
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

            String refreshToken = request.refreshToken();

            // Validate refresh token and get device info
            String refreshTokenKey = "refresh_token:" + refreshToken;
            Object tokenInfoObj = redisTemplate.opsForValue().get(refreshTokenKey);

            if (tokenInfoObj == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Invalid refresh token"
                ));
            }

            String tokenInfo = tokenInfoObj.toString();
            String[] parts = tokenInfo.split(":");
            String username = parts[0];
            String role = parts[1];
            String deviceType = parts.length > 2 ? parts[2] : "UNKNOWN";

            // Generate new token pair
            TokenPair newTokenPair = jwtUtil.generateTokenPair(username);

            // Delete old tokens
            redisTemplate.delete(refreshTokenKey);

            // Find old access token and delete it
            String oldAccessToken = authHeader.substring(7);
            redisTemplate.delete("access_token:" + oldAccessToken);

            // Store new tokens with device info
            redisTemplate.opsForValue().set(
                    "access_token:" + newTokenPair.accessToken(),
                    username + ":" + role + ":" + deviceType,
                    jwtUtil.getAccessExpiration() / 1000,
                    TimeUnit.SECONDS
            );

            redisTemplate.opsForValue().set(
                    "refresh_token:" + newTokenPair.refreshToken(),
                    username + ":" + role + ":" + deviceType,
                    jwtUtil.getRefreshExpiration() / 1000,
                    TimeUnit.SECONDS
            );

            // Update user session for this device
            String userSessionKey = "user_session:" + username + ":" + deviceType;
            redisTemplate.opsForValue().set(
                    userSessionKey,
                    newTokenPair.accessToken(),
                    jwtUtil.getAccessExpiration() / 1000,
                    TimeUnit.SECONDS
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Làm mới token thành công",
                    "data", Map.of(
                            "accessToken", newTokenPair.accessToken(),
                            "refreshToken", newTokenPair.refreshToken(),
                            "expireIn", EXPIRATION_TIME,
                            "deviceType", deviceType
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
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader,
                                   @RequestParam(value = "deviceType", required = false) String deviceType) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Authorization header is required"
                ));
            }

            String token = authHeader.substring(7);

            // Get token info to extract username and device type
            String tokenKey = "access_token:" + token;
            Object tokenInfoObj = redisTemplate.opsForValue().get(tokenKey);

            if (tokenInfoObj != null) {
                String tokenInfo = tokenInfoObj.toString();
                String[] parts = tokenInfo.split(":");
                String username = parts[0];
                String currentDeviceType = parts.length > 2 ? parts[2] : "UNKNOWN";

                // If deviceType is specified and equals "ALL", logout from all devices
                if ("ALL".equals(deviceType)) {
                    // Logout from all devices
                    // Logout from all devices
                    Set<Object> userSessionsObj = redisTemplate.keys("user_session:" + username + ":*");
                    Set<String> userSessions = userSessionsObj != null ?
                        userSessionsObj.stream().map(Object::toString).collect(Collectors.toSet()) :
                        Collections.emptySet();
                    if (userSessions != null) {
                        for (String sessionKey : userSessions) {
                            Object sessionToken = redisTemplate.opsForValue().get(sessionKey);
                            if (sessionToken != null) {
                                // Delete tokens for each session
                                redisTemplate.delete("access_token:" + sessionToken.toString());
                                redisTemplate.delete("refresh_token:" + sessionToken.toString());
                            }
                            // Delete session key
                            redisTemplate.delete(sessionKey);
                        }
                    }

                    log.info("User {} logged out from all devices", username);
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Logged out from all devices successfully"
                    ));
                } else {
                    // Logout from current device only
                    String userSessionKey = "user_session:" + username + ":" + currentDeviceType;

                    // Find refresh token to delete
                    // Find refresh token to delete
                    Set<Object> refreshTokenKeysObj = redisTemplate.keys("refresh_token:*");
                    Set<String> refreshTokenKeys = refreshTokenKeysObj != null ?
                        refreshTokenKeysObj.stream().map(Object::toString).collect(Collectors.toSet()) :
                        Collections.emptySet();
                    if (refreshTokenKeys != null) {
                        for (String refreshKey : refreshTokenKeys) {
                            Object refreshTokenInfo = redisTemplate.opsForValue().get(refreshKey);
                            if (refreshTokenInfo != null && refreshTokenInfo.toString().startsWith(username + ":")) {
                                String[] refreshParts = refreshTokenInfo.toString().split(":");
                                if (refreshParts.length > 2 && refreshParts[2].equals(currentDeviceType)) {
                                    redisTemplate.delete(refreshKey);
                                    break;
                                }
                            }
                        }
                    }

                    // Delete current session tokens
                    redisTemplate.delete(tokenKey);
                    redisTemplate.delete(userSessionKey);

                    log.info("User {} logged out from device: {}", username, currentDeviceType);
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Đăng xuất thành công",
                            "deviceType", currentDeviceType
                    ));
                }
            } else {
                // Token not found in Redis, but still return success
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Already logged out"
                ));
            }

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
                        "message", "Token hợp lệ",
                        "valid", true
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Token không hợp lệ",
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