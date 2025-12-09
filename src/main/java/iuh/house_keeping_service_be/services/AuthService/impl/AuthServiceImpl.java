package iuh.house_keeping_service_be.services.AuthService.impl;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Authentication.TokenPair;
import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.AdminProfile;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.models.Role;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.AdminProfileRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.repositories.RoleRepository;
import iuh.house_keeping_service_be.security.CustomUserDetailsService;
import iuh.house_keeping_service_be.services.AuthService.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

   @Autowired
   private AccountRepository accountRepository;

   @Autowired
   private RoleRepository roleRepository;

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

   @Autowired
   private CustomUserDetailsService customUserDetailsService;

   @Override
   public TokenPair login(String username, String password, String role, String deviceType) {
       log.info("Đăng nhập cho username: {}, role: {}, device: {}", username, role, deviceType);

       try {
           // Validate input
           if (username == null || username.trim().isEmpty()) {
               throw new IllegalArgumentException("Tên đăng nhập không được để trống");
           }
           if (password == null || password.isEmpty()) {
               throw new IllegalArgumentException("Mật khẩu không được để trống");
           }
           if (role == null || role.trim().isEmpty()) {
               throw new IllegalArgumentException("Vai trò không được để trống");
           }

           // Validate role
           RoleName requestedRole;
           try {
               requestedRole = RoleName.valueOf(role.toUpperCase().trim());
           } catch (IllegalArgumentException e) {
               throw new IllegalArgumentException("Vai trò không hợp lệ: " + role);
           }

           // Load user with role validation
           UserDetails userDetails = customUserDetailsService.loadUserByUsernameAndRole(username.trim(), requestedRole);

           // Authenticate
           authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(username.trim(), password)
           );

           // Generate tokens
           TokenPair tokenPair = jwtUtil.generateTokenPair(username.trim(), role.toUpperCase());

           // Store in Redis
           String accessTokenKey = "access_token:" + tokenPair.accessToken();
           String refreshTokenKey = "refresh_token:" + tokenPair.refreshToken();

           String redisValue = username.trim() + ":" + role.toUpperCase() + ":" + deviceType;

           redisTemplate.opsForValue().set(accessTokenKey, redisValue, 60, TimeUnit.MINUTES);
           redisTemplate.opsForValue().set(refreshTokenKey, redisValue, 7, TimeUnit.DAYS);

           // Update last login
           Account account = accountRepository.findByUsername(username.trim())
               .orElse(null);
           updateLastLoginTime(account);

           return tokenPair;
       } catch (Exception e) {
           log.error("Đăng nhập thất bại cho username: {}", username, e);
           throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage());
       }
   }

   @Override
   public Account register(String username, String password, String email, String phoneNumber, String role, String fullName) {
       log.info("Đăng ký tài khoản cho username: {}, email: {}, role: {}", username, email, role);

       // Validate input
       validateRegistrationInput(username, password, email, role, fullName, phoneNumber);

       // Check if username exists
       if (accountRepository.existsByUsername(username.trim())) {
           throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng");
       }

       // Check if phone number exists
       if (accountRepository.existsByPhoneNumber(phoneNumber.trim())) {
           throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
       }

       // Validate role
       RoleName roleName;
       try {
           roleName = RoleName.valueOf(role.toUpperCase().trim());
       } catch (IllegalArgumentException e) {
           throw new IllegalArgumentException("Vai trò không hợp lệ: " + role);
       }

       // Check email uniqueness for specific profiles
       checkEmailUniqueness(email, roleName);

       // Create account
       Account account = new Account();
       account.setUsername(username.trim());
       account.setPassword(passwordEncoder.encode(password));
       account.setPhoneNumber(phoneNumber.trim());
       account.setStatus(AccountStatus.ACTIVE);
       account.setIsPhoneVerified(false);

       // Get role entity
       Role roleEntity = roleRepository.findByRoleName(roleName)
           .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò: " + roleName));

       // Set roles
       Set<Role> roles = new HashSet<>();
       roles.add(roleEntity);
       account.setRoles(roles);

       // Save account
       account = accountRepository.save(account);

       // Create profile based on role
       createProfileForRole(account, roleName, fullName, email);

       return account;
   }

   private void createProfileForRole(Account account, RoleName roleName, String fullName, String email) {
       switch (roleName) {
           case CUSTOMER:
               Customer customer = new Customer();
               customer.setAccount(account);
               customer.setFullName(fullName);
               customer.setEmail(email.trim());
               customerRepository.save(customer);
               break;

           case EMPLOYEE:
               Employee employee = new Employee();
               employee.setAccount(account);
               employee.setFullName(fullName);
               employee.setEmail(email.trim());
               employee.setHiredDate(LocalDate.now());
               employeeRepository.save(employee);
               break;

           case ADMIN:
               AdminProfile adminProfile = new AdminProfile();
               adminProfile.setAccount(account);
               adminProfile.setFullName(fullName);
               adminProfile.setContactInfo(email.trim());
               adminProfile.setHireDate(LocalDate.now());
               adminProfileRepository.save(adminProfile);
               break;
       }
   }

   private void checkEmailUniqueness(String email, RoleName role) {
       switch (role) {
           case CUSTOMER:
               if (customerRepository.existsByEmail(email.trim())) {
                   throw new IllegalArgumentException("Email đã được sử dụng bởi khách hàng khác");
               }
               break;
           case EMPLOYEE:
               if (employeeRepository.existsByEmail(email.trim())) {
                   throw new IllegalArgumentException("Email đã được sử dụng bởi nhân viên khác");
               }
               break;
           case ADMIN:
               if (adminProfileRepository.existsByContactInfo(email.trim())) {
                   throw new IllegalArgumentException("Email đã được sử dụng bởi quản trị viên khác");
               }
               break;
       }
   }

   private void validateRegistrationInput(String username, String password, String email, String role, String fullName, String phoneNumber) {
       // Username validation
       if (username == null || username.trim().isEmpty()) {
           throw new IllegalArgumentException("Tên đăng nhập không được để trống");
       }
       if (username.trim().length() > 50) {
           throw new IllegalArgumentException("Tên đăng nhập không được quá 50 ký tự");
       }

       // Password validation
       if (password == null || password.isEmpty()) {
           throw new IllegalArgumentException("Mật khẩu không được để trống");
       }
       if (password.length() < 6) {
           throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
       }
       if (password.length() > 100) {
           throw new IllegalArgumentException("Mật khẩu không được quá 100 ký tự");
       }

       // Email validation
       if (email == null || email.trim().isEmpty()) {
           throw new IllegalArgumentException("Email không được để trống");
       }
       if (!email.trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
           throw new IllegalArgumentException("Email không đúng định dạng");
       }
       if (email.trim().length() > 255) {
           throw new IllegalArgumentException("Email không được quá 255 ký tự");
       }

       // Role validation
       if (role == null || role.trim().isEmpty()) {
           throw new IllegalArgumentException("Vai trò không được để trống");
       }
       if (!role.trim().matches("^(CUSTOMER|EMPLOYEE|ADMIN)$")) {
           throw new IllegalArgumentException("Vai trò không hợp lệ");
       }

       // Full name validation
       if (fullName == null || fullName.trim().isEmpty()) {
           throw new IllegalArgumentException("Họ tên không được để trống");
       }
       if (fullName.trim().length() > 100) {
           throw new IllegalArgumentException("Họ tên không được quá 100 ký tự");
       }

       // Phone number validation
       if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
           throw new IllegalArgumentException("Số điện thoại không được để trống");
       }
       if (!phoneNumber.trim().matches("^\\+?[0-9]{10,15}$")) {
           throw new IllegalArgumentException("Số điện thoại không đúng định dạng");
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
           throw new IllegalArgumentException("Refresh token không được để trống");
       }

       // Validate refresh token
       String refreshTokenKey = "refresh_token:" + refreshToken.trim();
       Object userInfoObj = redisTemplate.opsForValue().get(refreshTokenKey);

       if (userInfoObj == null) {
           throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
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
           15, TimeUnit.MINUTES
       );

       redisTemplate.opsForValue().set(
           "refresh_token:" + newTokenPair.refreshToken(),
           username + ":" + role + ":" + deviceType,
           7, TimeUnit.DAYS
       );

       return newTokenPair;
   }

   @Override
   public String logout(String token) {
       try {
           if (token == null || token.trim().isEmpty()) {
               throw new IllegalArgumentException("Token không được để trống");
           }

           String accessTokenKey = "access_token:" + token.trim();
           Object userInfoObj = redisTemplate.opsForValue().get(accessTokenKey);

           if (userInfoObj != null) {
               redisTemplate.delete(accessTokenKey);

               // Find and delete corresponding refresh token
               String userInfo = userInfoObj.toString();
               String[] parts = userInfo.split(":");
               String username = parts[0];

               Set<Object> refreshTokenKeys = redisTemplate.keys("refresh_token:*");
               for (Object key : refreshTokenKeys) {
                   Object refreshUserInfo = redisTemplate.opsForValue().get(key);
                   if (refreshUserInfo != null && refreshUserInfo.toString().startsWith(username + ":")) {
                       redisTemplate.delete(key);
                       break;
                   }
               }
           }

           return "Đăng xuất thành công";
       } catch (Exception e) {
           log.error("Lỗi khi đăng xuất", e);
           throw new RuntimeException("Đăng xuất thất bại: " + e.getMessage());
       }
   }

   @Override
   public String logoutAllDevices(String username) {
       try {
           if (username == null || username.trim().isEmpty()) {
               throw new IllegalArgumentException("Tên đăng nhập không được để trống");
           }

           // Delete all tokens for this user
           Set<Object> allKeys = redisTemplate.keys("*token:*");
           int deletedCount = 0;

           for (Object key : allKeys) {
               Object userInfo = redisTemplate.opsForValue().get(key);
               if (userInfo != null && userInfo.toString().startsWith(username.trim() + ":")) {
                   redisTemplate.delete(key);
                   deletedCount++;
               }
           }

           return "Đã đăng xuất khỏi " + deletedCount + " thiết bị";
       } catch (Exception e) {
           log.error("Lỗi khi đăng xuất tất cả thiết bị cho user: {}", username, e);
           throw new RuntimeException("Đăng xuất tất cả thiết bị thất bại: " + e.getMessage());
       }
   }

   @Override
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void updateLastLoginTime(Account account) {
       try {
           account.setLastLogin(LocalDateTime.now());
           accountRepository.save(account);
       } catch (Exception e) {
           log.error("Lỗi khi cập nhật thời gian đăng nhập cuối cho user: {}", account.getUsername(), e);
       }
   }

   @Override
   public void changePassword(String username, String currentPassword, String newPassword) {
       if (username == null || username.trim().isEmpty()) {
           throw new IllegalArgumentException("Tên đăng nhập không được để trống");
       }
       if (currentPassword == null || currentPassword.isEmpty()) {
           throw new IllegalArgumentException("Mật khẩu hiện tại không được để trống");
       }
       if (newPassword == null || newPassword.length() < 6) {
           throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
       }

       Account account = accountRepository.findByUsername(username.trim())
           .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

       if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
           throw new RuntimeException("Mật khẩu hiện tại không chính xác");
       }

       account.setPassword(passwordEncoder.encode(newPassword));
       accountRepository.save(account);
   }

   @Override
   public Map<String, String> getRole(String username, String password) {
       try {
           if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
               throw new IllegalArgumentException("Tên đăng nhập và mật khẩu không được để trống");
           }

           Account account = accountRepository.findByUsername(username.trim())
               .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

           if (!passwordEncoder.matches(password, account.getPassword())) {
               throw new RuntimeException("Thông tin đăng nhập không chính xác");
           }

           return account.getRoles().stream()
               .collect(Collectors.toMap(
                   role -> role.getRoleName().name(),
                   role -> account.getStatus().name(),
                   (existing, replacement) -> existing
               ));
       } catch (Exception e) {
           log.error("Lỗi khi lấy thông tin vai trò cho username: {}", username, e);
           throw new RuntimeException("Lấy thông tin vai trò thất bại: " + e.getMessage());
       }
   }
}