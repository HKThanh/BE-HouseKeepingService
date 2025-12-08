package iuh.house_keeping_service_be.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
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
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
    private static final int EXPIRATION_TIME = 3600; // 1 hour in seconds
    private static final int MAX_QR_PROCESS_DIMENSION = 2000;
    private static final int MAX_BASE_IMAGE_DIMENSION = 2200;
    private static final int MIN_CORNER_SIZE = 120;
    private static final int MAX_DECODE_ATTEMPTS = 1500;
    private static final long MAX_DECODE_DURATION_MS = 4500;
    private static final double[] GLOBAL_SCALE_FACTORS = {1.0, 1.5, 2.0, 2.5, 3.0};
    private static final double TOP_RIGHT_QUARTER_RATIO = 0.5;
    private static final double[] CORNER_RATIOS = {0.55, 0.4, 0.3, 0.2, 0.15};
    private static final double[] CORNER_ZOOM_FACTORS = {2.0, 3.2, 4.5, 6.0};
    private static final double[] DEFAULT_WINDOW_RATIOS = {0.7, 0.5, 0.35};
    private static final double[] TOP_RIGHT_WINDOW_RATIOS = {0.45, 0.32, 0.22, 0.15, 0.12};
    private static final int[] ROTATION_ANGLES = {0, 90, 180, 270};

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

    @Autowired
    private ObjectMapper objectMapper;

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
                        account.getAccountId(),
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
                        account.getAccountId(),
                        account.getUsername(),
                        customer.getAvatar(),
                        customer.getFullName(),
                        customer.getEmail(),
                        account.getPhoneNumber(),
                        customer.getIsMale(),
                        account.getStatus().name(),
                        address != null ? address.getFullAddress() : null,
                        customer.getIsEmailVerified(),
                        account.getIsPhoneVerified()
                );

            case EMPLOYEE:
                Employee employee = employeeService.findByAccountId(account.getAccountId());
                return new EmployeeLoginResponse(
                        employee.getEmployeeId(),
                        account.getAccountId(),
                        account.getUsername(),
                        employee.getAvatar(),
                        employee.getFullName(),
                        employee.getEmail(),
                        account.getPhoneNumber(),
                        employee.getIsMale(),
                        account.getStatus().name(),
                        employee.getHiredDate() != null ? employee.getHiredDate().toString() : null,
                        employee.getIsEmailVerified(),
                        account.getIsPhoneVerified()
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
                    registerRequest.role(),
                    false,
                    account.getIsPhoneVerified()
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

    @PostMapping("/decode-qr")
    public ResponseEntity<?> decodeQrFromImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Ảnh chứa QR không được để trống"
            ));
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            if (bufferedImage == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Định dạng ảnh không hợp lệ"
                ));
            }

            BufferedImage preparedImage = prepareImageForDecoding(bufferedImage);
            if (preparedImage == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                        "success", false,
                        "message", "Không thể chuẩn hóa ảnh để giải mã QR"
                ));
            }

            DecodeContext decodeContext = new DecodeContext();
            String decodedValue = decodeQrFromCandidates(preparedImage, decodeContext);

            if (decodedValue == null) {
                String failureMessage = decodeContext.isExpired()
                        ? "Không thể giải mã QR: ảnh quá lớn hoặc QR quá nhỏ. Vui lòng chụp gần hơn vào vùng QR."
                        : "Không thể giải mã QR từ ảnh cung cấp";
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                        "success", false,
                        "message", failureMessage
                ));
            }

            Object jsonPayload = parseJsonPayload(decodedValue);
            if (jsonPayload == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                        "success", false,
                        "message", "QR không chứa dữ liệu JSON hợp lệ"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Giải mã QR thành công",
                    "data", jsonPayload
            ));
        } catch (IOException e) {
            log.error("Could not read QR image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Không thể đọc ảnh QR"
            ));
        }
    }

    private Object parseJsonPayload(String decodedValue) {
        if (decodedValue == null || decodedValue.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(decodedValue);
        } catch (Exception ex) {
            log.warn("Decoded QR is not valid JSON: {}", ex.getMessage());
            return null;
        }
    }

    private BufferedImage prepareImageForDecoding(BufferedImage input) {
        BufferedImage rgbImage = ensureRgbImage(input);
        if (rgbImage == null) {
            return null;
        }

        int maxSide = Math.max(rgbImage.getWidth(), rgbImage.getHeight());
        if (maxSide <= MAX_BASE_IMAGE_DIMENSION) {
            return rgbImage;
        }

        double factor = (double) MAX_BASE_IMAGE_DIMENSION / maxSide;
        int targetWidth = Math.max(1, (int) Math.round(rgbImage.getWidth() * factor));
        int targetHeight = Math.max(1, (int) Math.round(rgbImage.getHeight() * factor));

        return resizeImage(rgbImage, targetWidth, targetHeight);
    }

    private String decodeTopRightQuarter(BufferedImage image, DecodeContext context) {
        if (image == null || context == null || !context.canContinue()) {
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        int cropWidth = Math.max((int) Math.round(width * TOP_RIGHT_QUARTER_RATIO), MIN_CORNER_SIZE);
        int cropHeight = Math.max((int) Math.round(height * TOP_RIGHT_QUARTER_RATIO), MIN_CORNER_SIZE);
        cropWidth = Math.min(cropWidth, width);
        cropHeight = Math.min(cropHeight, height);

        int startX = Math.max(0, width - cropWidth);
        int startY = 0;

        try {
            BufferedImage topRightQuarter = image.getSubimage(startX, startY, cropWidth, cropHeight);

            String decoded = decodeAllVariants(topRightQuarter, context);
            if (decoded != null) {
                return decoded;
            }

            decoded = decodeSlidingWindowCandidates(topRightQuarter, context, TOP_RIGHT_WINDOW_RATIOS);
            if (decoded != null) {
                return decoded;
            }

            for (double zoom : CORNER_ZOOM_FACTORS) {
                if (!context.canContinue()) {
                    return null;
                }
                BufferedImage zoomedQuarter = scaleImage(topRightQuarter, zoom);
                if (zoomedQuarter != topRightQuarter) {
                    decoded = decodeAllVariants(zoomedQuarter, context);
                    if (decoded != null) {
                        return decoded;
                    }

                    decoded = decodeSlidingWindowCandidates(zoomedQuarter, context, TOP_RIGHT_WINDOW_RATIOS);
                    if (decoded != null) {
                        return decoded;
                    }
                }
            }
        } catch (RasterFormatException ex) {
            log.warn("Top-right quarter crop failed: {}", ex.getMessage());
        }

        return null;
    }

    private String decodeQrFromCandidates(BufferedImage sourceImage, DecodeContext context) {
        if (sourceImage == null || context == null) {
            return null;
        }

        BufferedImage normalized = ensureRgbImage(sourceImage);

        for (int angle : ROTATION_ANGLES) {
            if (!context.canContinue()) {
                return null;
            }

            BufferedImage oriented = angle == 0 ? normalized : rotateImage(normalized, angle);
            String decoded = decodeOrientationPipeline(oriented, context);
            if (decoded != null) {
                return decoded;
            }
        }

        return null;
    }

    private String decodeOrientationPipeline(BufferedImage orientedImage, DecodeContext context) {
        if (orientedImage == null || context == null) {
            return null;
        }

        String prioritizedTopRight = decodeTopRightQuarter(orientedImage, context);
        if (prioritizedTopRight != null) {
            return prioritizedTopRight;
        }

        for (double factor : GLOBAL_SCALE_FACTORS) {
            if (!context.canContinue()) {
                return null;
            }
            BufferedImage workingImage = factor == 1.0 ? orientedImage : scaleImage(orientedImage, factor);

            String zoomedTopRight = decodeTopRightQuarter(workingImage, context);
            if (zoomedTopRight != null) {
                return zoomedTopRight;
            }

            String decoded = decodeAllVariants(workingImage, context);
            if (decoded != null) {
                return decoded;
            }

            decoded = decodeCornerCandidates(workingImage, context);
            if (decoded != null) {
                return decoded;
            }

            decoded = decodeSlidingWindowCandidates(workingImage, context, DEFAULT_WINDOW_RATIOS);
            if (decoded != null) {
                return decoded;
            }
        }

        return null;
    }

    private String decodeCornerCandidates(BufferedImage sourceImage, DecodeContext context) {
        if (sourceImage == null || context == null) {
            return null;
        }

        for (double ratio : CORNER_RATIOS) {
            if (!context.canContinue()) {
                return null;
            }
            int cropWidth = Math.min(sourceImage.getWidth(), Math.max((int) (sourceImage.getWidth() * ratio), MIN_CORNER_SIZE));
            int cropHeight = Math.min(sourceImage.getHeight(), Math.max((int) (sourceImage.getHeight() * ratio), MIN_CORNER_SIZE));

            try {
                BufferedImage topLeft = sourceImage.getSubimage(0, 0, cropWidth, cropHeight);
                BufferedImage topRight = sourceImage.getSubimage(sourceImage.getWidth() - cropWidth, 0, cropWidth, cropHeight);
                BufferedImage bottomLeft = sourceImage.getSubimage(0, sourceImage.getHeight() - cropHeight, cropWidth, cropHeight);
                BufferedImage bottomRight = sourceImage.getSubimage(sourceImage.getWidth() - cropWidth, sourceImage.getHeight() - cropHeight, cropWidth, cropHeight);

                List<BufferedImage> corners = Arrays.asList(topLeft, topRight, bottomLeft, bottomRight);
                for (BufferedImage corner : corners) {
                    if (!context.canContinue()) {
                        return null;
                    }
                    String decoded = decodeAllVariants(corner, context);
                    if (decoded != null) {
                        return decoded;
                    }

                    for (double zoom : CORNER_ZOOM_FACTORS) {
                        if (!context.canContinue()) {
                            return null;
                        }
                        BufferedImage zoomedCorner = scaleImage(corner, zoom);
                        if (zoomedCorner != corner) {
                            decoded = decodeAllVariants(zoomedCorner, context);
                            if (decoded != null) {
                                return decoded;
                            }
                        }
                    }
                }
            } catch (RasterFormatException ex) {
                log.warn("Corner crop failed: {}", ex.getMessage());
            }
        }

        return null;
    }

    private BufferedImage scaleImage(BufferedImage source, double factor) {
        if (source == null || factor <= 1.0) {
            return source;
        }

        double limiter = Math.min(
                (double) MAX_QR_PROCESS_DIMENSION / source.getWidth(),
                (double) MAX_QR_PROCESS_DIMENSION / source.getHeight()
        );

        double appliedFactor = Math.min(factor, limiter);

        int targetWidth = (int) Math.round(source.getWidth() * appliedFactor);
        int targetHeight = (int) Math.round(source.getHeight() * appliedFactor);

        if (targetWidth <= source.getWidth() || targetHeight <= source.getHeight()) {
            return source;
        }

        return resizeImage(source, targetWidth, targetHeight);
    }

    private BufferedImage resizeImage(BufferedImage source, int targetWidth, int targetHeight) {
        if (source == null || targetWidth <= 0 || targetHeight <= 0) {
            return source;
        }

        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return scaledImage;
    }

    private String decodeSlidingWindowCandidates(BufferedImage image, DecodeContext context, double[] windowRatios) {
        if (image == null || context == null) {
            return null;
        }

        int minDimension = Math.min(image.getWidth(), image.getHeight());
        if (minDimension <= 0) {
            return null;
        }

        double[] ratiosToUse = (windowRatios == null || windowRatios.length == 0)
                ? DEFAULT_WINDOW_RATIOS
                : windowRatios;

        for (double ratio : ratiosToUse) {
            if (!context.canContinue()) {
                return null;
            }

            int windowSize = Math.min(Math.max((int) Math.round(minDimension * ratio), MIN_CORNER_SIZE), minDimension);
            if (windowSize <= 0) {
                continue;
            }

            int step = Math.max(windowSize / 2, Math.max(MIN_CORNER_SIZE / 2, 20));

            List<Integer> xPositions = computePositions(image.getWidth(), windowSize, step);
            List<Integer> yPositions = computePositions(image.getHeight(), windowSize, step);

            for (int y : yPositions) {
                if (!context.canContinue()) {
                    return null;
                }
                for (int x : xPositions) {
                    if (!context.canContinue()) {
                        return null;
                    }
                    try {
                        BufferedImage window = image.getSubimage(x, y, windowSize, windowSize);
                        String decoded = decodeAllVariants(window, context);
                        if (decoded != null) {
                            return decoded;
                        }
                    } catch (RasterFormatException ex) {
                        log.warn("Sliding window crop failed: {}", ex.getMessage());
                    }
                }
            }
        }

        return null;
    }

    private List<Integer> computePositions(int length, int windowSize, int step) {
        List<Integer> positions = new ArrayList<>();
        if (length <= 0 || windowSize <= 0) {
            return positions;
        }

        if (windowSize >= length) {
            positions.add(0);
            return positions;
        }

        int pos = 0;
        while (pos <= length - windowSize) {
            positions.add(pos);
            pos += step;
        }

        int lastStart = length - windowSize;
        if (positions.get(positions.size() - 1) != lastStart) {
            positions.add(lastStart);
        }

        return positions;
    }

    private String decodeAllVariants(BufferedImage image, DecodeContext context) {
        if (image == null || context == null) {
            return null;
        }

        BufferedImage normalized = ensureRgbImage(image);
        for (int angle : ROTATION_ANGLES) {
            if (!context.canContinue()) {
                return null;
            }
            BufferedImage rotated = angle == 0 ? normalized : rotateImage(normalized, angle);
            String decoded = decodeCandidateVariant(rotated, context);
            if (decoded != null) {
                return decoded;
            }

            BufferedImage gray = convertToGray(rotated);
            decoded = decodeCandidateVariant(gray, context);
            if (decoded != null) {
                return decoded;
            }
        }

        return null;
    }

    private String decodeCandidateVariant(BufferedImage candidate, DecodeContext context) {
        if (candidate == null || context == null || !context.canContinue()) {
            return null;
        }

        context.recordAttempt();
        String decoded = normalizeDecodedText(decodeCandidate(candidate));
        if (decoded != null) {
            context.markSuccess();
        }
        return decoded;
    }

    private String normalizeDecodedText(String decodedText) {
        if (decodedText == null) {
            return null;
        }

        String trimmed = decodedText.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BufferedImage ensureRgbImage(BufferedImage source) {
        if (source == null) {
            return null;
        }

        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }

        BufferedImage rgbImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = rgbImage.createGraphics();
        graphics2D.drawImage(source, 0, 0, null);
        graphics2D.dispose();
        return rgbImage;
    }

    private BufferedImage convertToGray(BufferedImage source) {
        if (source == null) {
            return null;
        }

        BufferedImage grayImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics2D = grayImage.createGraphics();
        graphics2D.drawImage(source, 0, 0, null);
        graphics2D.dispose();
        return grayImage;
    }

    private BufferedImage rotateImage(BufferedImage source, int angle) {
        if (source == null || angle % 360 == 0) {
            return source;
        }

        int width = source.getWidth();
        int height = source.getHeight();
        int targetWidth = angle % 180 == 0 ? width : height;
        int targetHeight = angle % 180 == 0 ? height : width;

        BufferedImage rotatedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = rotatedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        double radians = Math.toRadians(angle);

        switch (angle % 360) {
            case 90:
                graphics2D.translate(targetWidth, 0);
                break;
            case 180:
                graphics2D.translate(targetWidth, targetHeight);
                break;
            case 270:
                graphics2D.translate(0, targetHeight);
                break;
            default:
                graphics2D.translate(targetWidth / 2.0, targetHeight / 2.0);
                graphics2D.rotate(radians);
                graphics2D.translate(-width / 2.0, -height / 2.0);
                graphics2D.drawImage(source, 0, 0, null);
                graphics2D.dispose();
                return rotatedImage;
        }

        graphics2D.rotate(radians);
        graphics2D.drawImage(source, 0, 0, null);
        graphics2D.dispose();
        return rotatedImage;
    }

    private String decodeCandidate(BufferedImage candidate) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(candidate);

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

            BinaryBitmap hybridBitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                Result result = new MultiFormatReader().decode(hybridBitmap, hints);
                if (result != null) {
                    return result.getText();
                }
            } catch (NotFoundException ignored) {
            }

            BinaryBitmap histogramBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            Result histogramResult = new MultiFormatReader().decode(histogramBitmap, hints);
            return histogramResult.getText();
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            log.warn("QR decode attempt failed: {}", e.getMessage());
            return null;
        }
    }

    private static class DecodeContext {
        private final long startTimeNs = System.nanoTime();
        private int attempts;
        private boolean success;

        boolean canContinue() {
            return !success && !isExpiredInternal();
        }

        void recordAttempt() {
            attempts++;
        }

        void markSuccess() {
            success = true;
        }

        boolean isExpired() {
            return isExpiredInternal();
        }

        private boolean isExpiredInternal() {
            return attempts >= MAX_DECODE_ATTEMPTS || elapsedMs() > MAX_DECODE_DURATION_MS;
        }

        private long elapsedMs() {
            return (System.nanoTime() - startTimeNs) / 1_000_000;
        }
    }
}
