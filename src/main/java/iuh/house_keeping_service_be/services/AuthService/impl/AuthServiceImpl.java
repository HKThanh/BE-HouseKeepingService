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
import java.util.List;
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
            TokenPair tokenPair = jwtUtil.generateTokenPair(username.trim(), account.getRole().name());
    
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
    public Account register(String username, String password, String email, String phoneNumber, String role, String fullName) {
        log.info("Registration attempt for username: {}, email: {}, role: {}", username, email, role);

        try {
            // Input validation
            validateRegistrationInput(username, password, email, role, fullName, phoneNumber);

            // Check if username already exists
            if (accountRepository.findByUsername(username.trim()).isPresent()) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng");
            }

            // Validate and convert role string to enum
            Role accountRole;
            try {
                accountRole = Role.valueOf(role.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Role không hợp lệ");
            }

            // Check email uniqueness based on role
            checkEmailUniqueness(email.trim(), accountRole);

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

            // Save account first
            Account savedAccount = accountRepository.save(account);

            // Create specific user type based on role
            switch (accountRole) {
                case CUSTOMER -> {
                    Customer customer = new Customer();
                    customer.setCustomerId(UUID.randomUUID().toString());
                    customer.setAccount(savedAccount);
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
                    employee.setAccount(savedAccount);
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
                    admin.setAccount(savedAccount);
                    admin.setFullName(fullName.trim()); // Added full name
                    admin.setContactInfo(email.trim());
                    admin.setHireDate(LocalDate.now());
                    admin.setCreatedAt(Instant.now());
                    admin.setUpdatedAt(Instant.now());
                    adminProfileRepository.save(admin);
                }
                default -> throw new IllegalArgumentException("Unsupported role: " + accountRole);
            }

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

    private void checkEmailUniqueness(String email, Role role) {
        switch (role) {
            case CUSTOMER -> {
                if (customerRepository.existsByEmail(email)) {
                    throw new IllegalArgumentException("Đã có khách hàng dùng email này");
                }
            }
            case EMPLOYEE -> {
                if (employeeRepository.existsByEmail(email)) {
                    throw new IllegalArgumentException("Đã có nhân viên dùng email này");
                }
            }
            case ADMIN -> {
                if (adminProfileRepository.existsByContactInfo(email)) {
                    throw new IllegalArgumentException("Đã có admin dùng email này");
                }
            }
        }
    }

    private void validateRegistrationInput(String username, String password, String email, String role, String fullName, String phoneNumber) {
        // Username validation
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được thiếu");
        }
        if (username.trim().length() < 3 || username.trim().length() > 50) {
            throw new IllegalArgumentException("Tên đăng nhập phải có từ 3 đến 50 ký tự");
        }
        if (!username.trim().matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới");
        }

        // Password validation
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được thiếu");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (password.length() > 100) {
            throw new IllegalArgumentException("Mật khẩu không được vượt quá 100 ký tự");
        }

        // Email validation
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được thiếu");
        }
        if (!email.trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ");
        }
        if (email.trim().length() > 255) {
            throw new IllegalArgumentException("Email không được vượt quá 255 ký tự");
        }

        // Role validation
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Vai trò không được thiếu");
        }
        if (!role.trim().matches("^(CUSTOMER|EMPLOYEE|ADMIN)$")) {
            throw new IllegalArgumentException("Vai trò phải là CUSTOMER, EMPLOYEE hoặc ADMIN");
        }

        // Full name validation
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được thiếu");
        }
        if (fullName.trim().length() > 100) {
            throw new IllegalArgumentException("Họ và tên không được vượt quá 100 ký tự");
        }
        if (!fullName.trim().matches("^[a-zA-ZÀ-ỹ\\s]+$")) {
            throw new IllegalArgumentException("Họ và tên chỉ được chứa chữ cái và khoảng trắng");
        }

        // Phone number validation
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được thiếu");
        }
        if (!phoneNumber.trim().matches("^\\+?[0-9]{10,15}$")) {
            throw new IllegalArgumentException("Định dạng số điện thoại không hợp lệ");
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            // Check if token exists in Redis with correct key pattern
            String tokenKey = "access_token:" + token.trim();
            Object userInfoObj = redisTemplate.opsForValue().get(tokenKey);

            // Check if token exists
            if (userInfoObj == null) {
                log.debug("Token not found in Redis: {}", token);
                return false;
            }

            String userInfo = userInfoObj.toString();
            String[] parts = userInfo.split(":");
            String username = parts[0];

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
            throw new IllegalArgumentException("Refresh token là bắt buộc");
        }

        // Validate refresh token
        String refreshTokenKey = "refresh_token:" + refreshToken.trim();
        Object userInfoObj = redisTemplate.opsForValue().get(refreshTokenKey);

        if (userInfoObj == null) {
            throw new RuntimeException("Refresh token không hợp lệ");
        }

        String userInfo = userInfoObj.toString();
        String[] parts = userInfo.split(":");
        String username = parts[0];
        String role = parts[1];
        String deviceType = parts.length > 2 ? parts[2] : "UNKNOWN";

        // Generate new token pair
        TokenPair newTokenPair = jwtUtil.generateTokenPair(username, role);

        // Delete old refresh token
        redisTemplate.delete(refreshTokenKey);

        // Store new tokens in Redis with device info
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
//    private void validateRegistrationInput(String username, String password, String email, String role, String fullName) {
//        if (username == null || username.trim().isEmpty()) {
//            throw new IllegalArgumentException("Username is required");
//        }
//        if (password == null || password.length() < 6) {
//            throw new IllegalArgumentException("Password must be at least 6 characters");
//        }
//        if (email == null || !email.contains("@")) {
//            throw new IllegalArgumentException("Valid email is required");
//        }
//        if (role == null || role.trim().isEmpty()) {
//            throw new IllegalArgumentException("Role is required");
//        }
//        if (fullName == null || fullName.trim().isEmpty()) {
//            throw new IllegalArgumentException("Full name is required");
//        }
//    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastLoginTime(Account account) {
        // Fetch a fresh managed entity to avoid detached entity issues
        Account managedAccount = accountRepository.findById(account.getAccountId().toString())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        managedAccount.setLastLogin(Instant.now());
        accountRepository.saveAndFlush(managedAccount);
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được thiếu");
        }
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không được thiếu");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu mới không được thiếu");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        if (newPassword.length() > 50) {
            throw new IllegalArgumentException("Mật khẩu mới không được vượt quá 50 ký tự");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        if (!newPassword.matches("^[\\x20-\\x7E]*$")) {
            throw new IllegalArgumentException("Mật khẩu mới chứa ký tự không hợp lệ");
        }

        // Password strength validation (optional)
        if (!newPassword.matches(".*[a-zA-Z].*")) {
            throw new IllegalArgumentException("Mật khẩu mới phải chứa ít nhất một chữ cái");
        }

        // Find account
        Account account = accountRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        // Update password
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);

        log.info("Password changed successfully for user: {}", username);
    }

    @Override
    public String getRole(String username, String password) {
        try {
            // Validate input
            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Phải nhập tài khoản và mật khẩu");
            }

            // Find all accounts with the same username
            List<Account> accounts = accountRepository.findAccountsByUsername(username.trim());

            if (accounts.isEmpty()) {
                throw new IllegalArgumentException("Tài khoản không tồn tại");
            }

            // Verify password against the first account (since all should have same password)
            Account firstAccount = accounts.get(0);
            if (!passwordEncoder.matches(password, firstAccount.getPassword())) {
                throw new IllegalArgumentException("Mật khẩu không đúng");
            }

            log.info("Found {} roles for user: {}", accounts.size(), username);

            // Return all roles connected by ','
            return accounts.stream()
                    .map(Account::getRole)
                    .map(Role::name)
                    .distinct() // Remove duplicates if any
                    .reduce((role1, role2) -> role1 + "," + role2)
                    .orElse("");

        } catch (Exception e) {
            log.error("Error getting role for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Xác thực thất bại: " + e.getMessage());
        }
    }
}