package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Authentication.*;
import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.AddressService.AddressService;
import iuh.house_keeping_service_be.services.AdminService.AdminService;
import iuh.house_keeping_service_be.services.AuthService.AuthService;
import iuh.house_keeping_service_be.services.CustomerService.CustomerService;
import iuh.house_keeping_service_be.services.EmployeeService.EmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private AuthService authServiceImpl;

    @Autowired
    private AddressService addressService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.username();
        String password = loginRequest.password();
        String requestedRole = loginRequest.role();
        String deviceType = loginRequest.deviceType();

        log.info("Login attempt for username: {}, role: {}, device: {}", username, requestedRole, deviceType);

        // Input validation
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
                // Get token pair first
                TokenPair tokenPair = authService.login(username, password, requestedRole, deviceType);

                List<Account> accounts = accountRepository.findAccountsByUsernameAndRole(username.trim(), RoleName.valueOf(requestedRole.toUpperCase()));

                if (accounts.isEmpty()) {
                    incrementFailedLoginAttempts(username);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                            "success", false,
                            "message", "Thông tin đăng nhập không hợp lệ"
                    ));
                }

                Account account = accounts.get(0);

                if (account.getStatus() != AccountStatus.ACTIVE) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                            "success", false,
                            "message", "Tài khoản chưa được kích hoạt hoặc đã bị khóa"
                    ));
                }

                RoleName role = RoleName.valueOf(requestedRole.toUpperCase());

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

                // Store access token
                redisTemplate.opsForValue().set(
                        "access_token:" + tokenPair.accessToken(),
                        username + ":" + role.name() + ":" + deviceType,
                        jwtUtil.getAccessExpiration() / 1000,
                        TimeUnit.SECONDS
                );

                // Store refresh token
                redisTemplate.opsForValue().set(
                        "refresh_token:" + tokenPair.refreshToken(),
                        username + ":" + role.name() + ":" + deviceType,
                        jwtUtil.getRefreshExpiration() / 1000,
                        TimeUnit.SECONDS
                );

                // Fetch user profile data
                Object userData = getUserData(account, role);

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
                        "data", userData
                );

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đăng nhập thành công",
                        "data", responseData
                ));

            } catch (RuntimeException re) {
                incrementFailedLoginAttempts(username);
                log.error("Login error for user: {}, error: {}", username, re.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Tài khoản hoặc mật khẩu không hợp lệ"
                ));
            } catch (Exception e) {
                incrementFailedLoginAttempts(username);
                log.warn("Authentication failed for user: {}, error: {}", username, e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Thông tin đăng nhập không hợp lệ"
                ));
            }

        } catch (Exception e) {
            log.error("Login failed for user: {}, error: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi đăng nhập"
            ));
        }
    }

    private Object getUserData(Account account, RoleName role) {
        switch (role) {
            case ADMIN:
                AdminProfile adminProfile = adminService.findByAccountId(account.getAccountId());
                return new AdminLoginResponse(
                        adminProfile.getAdminProfileId(),
                        account.getUsername(),
                        adminProfile.getFullName(),
                        adminProfile.getIsMale(),
                        adminProfile.getDepartment(),
                        adminProfile.getContactInfo(),
                        adminProfile.getHireDate().toString()
                );

            case CUSTOMER:
                Customer customer = customerService.findByAccountId(account.getAccountId());

                Address address = addressService.findByCustomerId(customer.getCustomerId());
                return new CustomerLoginResponse(
                        customer.getCustomerId(),
                        account.getUsername(),
                        customer.getAvatar(),
                        customer.getFullName(),
                        customer.getEmail(),
                        account.getPhoneNumber(),
                        customer.getIsMale(),
                        account.getStatus().name(),
                        address != null ? address.getFullAddress() : null
                );

            case EMPLOYEE:
                Employee employee = employeeService.findByAccountId(account.getAccountId());
                return new EmployeeLoginResponse(
                        employee.getEmployeeId(),
                        account.getUsername(),
                        employee.getAvatar(),
                        employee.getFullName(),
                        employee.getEmail(),
                        account.getPhoneNumber(),
                        employee.getIsMale(),
                        account.getStatus().name(),
                        employee.getHiredDate() != null ? employee.getHiredDate().toString() : null
                );

            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Tracks failed login attempts and locks accounts after multiple failures
     */
    private void incrementFailedLoginAttempts(String username) {
        try {
            String failedKey = "login:failed:" + username;
            String lockKey = "login:locked:" + username;

            Long attempts = redisTemplate.opsForValue().increment(failedKey);
            redisTemplate.expire(failedKey, 600, TimeUnit.SECONDS); // Set expiration for failed attempts

            // Lock account after 3 failed attempts
            if (attempts != null && attempts >= 3) {
                // Lock for 10 minutes
                redisTemplate.opsForValue().set(lockKey, "locked", 600, TimeUnit.SECONDS);
                redisTemplate.delete(failedKey);
                log.warn("Account locked due to multiple failed login attempts: {}", username);
            }
        } catch (Exception e) {
            log.error("Error incrementing failed login attempts for {}: {}", username, e.getMessage());
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
            Set<Object> mobileSessionsObj = redisTemplate.keys("user_session:" + username + ":MOBILE");

            int webSessionCount = webSessionsObj != null ? webSessionsObj.size() : 0;
            int mobileSessionCount = mobileSessionsObj != null ? mobileSessionsObj.size() : 0;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "webSessions", webSessionCount,
                            "mobileSessions", mobileSessionCount
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
            String deviceType = parts.length > 2 ? parts[2] : "WEB";

            // Generate new token pair
            TokenPair newTokenPair = jwtUtil.generateTokenPair(username, role);

            // Delete old tokens
            redisTemplate.delete(refreshTokenKey);
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
                String currentDeviceType = parts.length > 2 ? parts[2] : "WEB";

                // If deviceType is specified and equals "ALL", logout from all devices
                if ("ALL".equals(deviceType)) {
                    // Logout from all devices
                    Set<Object> userSessionsObj = redisTemplate.keys("user_session:" + username + ":*");
                    if (userSessionsObj != null) {
                        for (Object sessionKey : userSessionsObj) {
                            Object sessionToken = redisTemplate.opsForValue().get(sessionKey.toString());
                            if (sessionToken != null) {
                                redisTemplate.delete("access_token:" + sessionToken.toString());
                            }
                            redisTemplate.delete(sessionKey.toString());
                        }
                    }

                    // Delete all refresh tokens for this user
                    Set<Object> refreshTokenKeysObj = redisTemplate.keys("refresh_token:*");
                    if (refreshTokenKeysObj != null) {
                        for (Object refreshKey : refreshTokenKeysObj) {
                            Object refreshTokenInfo = redisTemplate.opsForValue().get(refreshKey.toString());
                            if (refreshTokenInfo != null && refreshTokenInfo.toString().startsWith(username + ":")) {
                                redisTemplate.delete(refreshKey.toString());
                            }
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

                    // Find and delete refresh token
                    Set<Object> refreshTokenKeysObj = redisTemplate.keys("refresh_token:*");
                    if (refreshTokenKeysObj != null) {
                        for (Object refreshKey : refreshTokenKeysObj) {
                            Object refreshTokenInfo = redisTemplate.opsForValue().get(refreshKey.toString());
                            if (refreshTokenInfo != null) {
                                String[] refreshParts = refreshTokenInfo.toString().split(":");
                                if (refreshParts.length >= 3 &&
                                        refreshParts[0].equals(username) &&
                                        refreshParts[2].equals(currentDeviceType)) {
                                    redisTemplate.delete(refreshKey.toString());
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
                    "message", "Token không hợp lệ",
                    "valid", false
            ));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Authorization header is required"
                ));
            }

            String token = authHeader.substring(7);

            // Validate token first
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Token không hợp lệ"
                ));
            }

            // Extract username from token
            String username = jwtUtil.extractUsername(token);

            // Validate password confirmation
            if (!request.newPassword().equals(request.confirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Mật khẩu xác nhận không khớp",
                        "field", "confirmPassword"
                ));
            }

            // Change password
            authService.changePassword(username, request.currentPassword(), request.newPassword());

            // Logout all devices after password change for security
            Set<Object> userSessionsObj = redisTemplate.keys("user_session:" + username + ":*");
            if (userSessionsObj != null) {
                for (Object sessionKey : userSessionsObj) {
                    Object sessionToken = redisTemplate.opsForValue().get(sessionKey.toString());
                    if (sessionToken != null) {
                        redisTemplate.delete("access_token:" + sessionToken.toString());
                    }
                    redisTemplate.delete(sessionKey.toString());
                }
            }

            // Delete all refresh tokens for this user
            Set<Object> refreshTokenKeysObj = redisTemplate.keys("refresh_token:*");
            if (refreshTokenKeysObj != null) {
                for (Object refreshKey : refreshTokenKeysObj) {
                    Object refreshTokenInfo = redisTemplate.opsForValue().get(refreshKey.toString());
                    if (refreshTokenInfo != null && refreshTokenInfo.toString().startsWith(username + ":")) {
                        redisTemplate.delete(refreshKey.toString());
                    }
                }
            }

            log.info("Password changed successfully for user: {}", username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đổi mật khẩu thành công"
            ));

        } catch (IllegalArgumentException e) {
            String field = extractFieldFromPasswordError(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "field", field
            ));
        } catch (Exception e) {
            log.error("Change password error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi đổi mật khẩu"
            ));
        }
    }

    private String extractFieldFromPasswordError(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("mật khẩu hiện tại")) return "currentPassword";
        if (lowerMessage.contains("mật khẩu mới")) return "newPassword";
        if (lowerMessage.contains("xác nhận")) return "confirmPassword";
        if (lowerMessage.contains("tên đăng nhập")) return "username";
        if (lowerMessage.contains("ký tự không hợp lệ")) return "newPassword";
        if (lowerMessage.contains("chứa ít nhất một chữ cái")) return "newPassword";

        return "general";
    }

    @PostMapping("/get-role")
    public ResponseEntity<?> getRole(@Valid @RequestBody GetRoleRequest request) {
        try {
            String username = request.username();
            String password = request.password();

            // Validate input
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Tên đăng nhập không được để trống"
                ));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Mật khẩu không được để trống"
                ));
            }

            // Find account by username
            List<Account> accounts = accountRepository.findAccountsByUsername(username.trim());

            if (accounts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Tài khoản không tồn tại"
                ));
            }

            // Get roles and their statuses
            Map<String, String> roleData;
            try {
                roleData = authService.getRole(username.trim(), password);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Mật khẩu không chính xác"
                ));
            }

            int roleNumbers = roleData.size();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy vai trò thành công",
                    "data", roleData,
                    "roleNumbers", roleNumbers
            ));

        } catch (Exception e) {
            log.error("Get role error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy vai trò"
            ));
        }
    }
}