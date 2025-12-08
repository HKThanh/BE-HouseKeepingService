package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Otp.*;
import iuh.house_keeping_service_be.enums.OtpType;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.services.OtpService.OtpService;
import iuh.house_keeping_service_be.services.EmailOtpService.EmailOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý các API liên quan đến OTP
 * Sử dụng Firebase Authentication để gửi và xác thực OTP
 */
@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
@Slf4j
public class OtpController {

    private final OtpService otpService;
    private final EmailOtpService emailOtpService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Gửi OTP đến số điện thoại
     * Sử dụng cho: đăng ký, quên mật khẩu, xác thực số điện thoại
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        try {
            String phoneNumber = request.phoneNumber();
            OtpType otpType = OtpType.valueOf(request.otpType().toUpperCase());

            // Validate dựa trên loại OTP
            validateSendOtpRequest(phoneNumber, otpType);

            // Kiểm tra cooldown
            long cooldown = otpService.getResendCooldown(phoneNumber, otpType);
            if (cooldown > 0) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "success", false,
                    "message", "Vui lòng chờ " + cooldown + " giây trước khi gửi lại OTP",
                    "resendAfterSeconds", cooldown
                ));
            }

            // Gửi OTP
            String sessionInfo = otpService.sendOtp(phoneNumber, otpType, request.recaptchaToken());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mã OTP đã được gửi đến số điện thoại của bạn",
                "sessionInfo", sessionInfo,
                "expirationSeconds", 300, // 5 minutes
                "resendAfterSeconds", 60
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Send OTP validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            log.warn("Send OTP state error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Send OTP error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Không thể gửi mã OTP. Vui lòng thử lại sau"
            ));
        }
    }

    /**
     * Xác minh OTP
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            String phoneNumber = request.phoneNumber();
            String otp = request.otp();
            OtpType otpType = OtpType.valueOf(request.otpType().toUpperCase());
            String sessionInfo = request.sessionInfo();

            // Xác minh OTP
            String verificationToken = otpService.verifyOtp(phoneNumber, otp, otpType, sessionInfo);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xác thực OTP thành công",
                "verificationToken", verificationToken
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Verify OTP error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            log.warn("Verify OTP state error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Verify OTP error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi xác thực OTP"
            ));
        }
    }

    /**
     * Đặt lại mật khẩu sau khi xác thực OTP thành công
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String phoneNumber = request.phoneNumber();
            String verificationToken = request.verificationToken();
            String newPassword = request.newPassword();
            String role = request.role();

            // Validate token
            if (!otpService.validateVerificationToken(phoneNumber, verificationToken, OtpType.FORGOT_PASSWORD)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token xác thực không hợp lệ hoặc đã hết hạn"
                ));
            }

            // Tìm tài khoản theo số điện thoại
            String normalizedPhone = normalizePhoneNumber(phoneNumber);
            Optional<Account> accountOpt = accountRepository.findByPhoneNumber(normalizedPhone);

            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy tài khoản với số điện thoại này"
                ));
            }

            Account account = accountOpt.get();

            // Kiểm tra vai trò
            boolean hasRole = account.getRoles().stream()
                .anyMatch(r -> r.getRoleName().name().equalsIgnoreCase(role));

            if (!hasRole) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Tài khoản không có vai trò " + role
                ));
            }

            // Cập nhật mật khẩu
            account.setPassword(passwordEncoder.encode(newPassword));
            accountRepository.save(account);

            // Xóa token xác thực
            otpService.invalidateVerificationToken(phoneNumber, OtpType.FORGOT_PASSWORD);

            log.info("Password reset successful for phone: {}", maskPhoneNumber(normalizedPhone));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đặt lại mật khẩu thành công"
            ));

        } catch (Exception e) {
            log.error("Reset password error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi đặt lại mật khẩu"
            ));
        }
    }

    /**
     * Kiểm tra trạng thái cooldown gửi lại OTP
     */
    @GetMapping("/resend-status")
    public ResponseEntity<?> getResendStatus(
            @RequestParam String phoneNumber,
            @RequestParam String otpType) {
        try {
            OtpType type = OtpType.valueOf(otpType.toUpperCase());
            long cooldown = otpService.getResendCooldown(phoneNumber, type);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "canResend", cooldown == 0,
                "resendAfterSeconds", cooldown
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Loại OTP không hợp lệ"
            ));
        } catch (Exception e) {
            log.error("Get resend status error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi"
            ));
        }
    }

    /**
     * Kiểm tra trạng thái Firebase OTP service
     */
    @GetMapping("/status")
    public ResponseEntity<?> getServiceStatus() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "enabled", otpService.isEnabled(),
            "provider", "firebase"
        ));
    }

    /**
     * Gửi OTP đến email
     * Sử dụng cho: xác thực email
     */
    @PostMapping("/email/send")
    public ResponseEntity<?> sendEmailOtp(@Valid @RequestBody SendEmailOtpRequest request) {
        try {
            String email = request.email();
            
            // Gửi OTP email
            emailOtpService.sendEmailOtp(email);
            
            long cooldownSeconds = emailOtpService.getResendCooldownSeconds(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mã OTP đã được gửi đến email của bạn",
                "expirationSeconds", 180, // 3 minutes
                "cooldownSeconds", cooldownSeconds
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("Send email OTP validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Send email OTP error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Không thể gửi mã OTP. Vui lòng thử lại sau"
            ));
        }
    }
    
    /**
     * Xác thực OTP email
     */
    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmailOtp(@Valid @RequestBody VerifyEmailOtpRequest request) {
        try {
            String email = request.email();
            String otp = request.otp();
            
            // Xác thực OTP email
            emailOtpService.verifyEmailOtp(email, otp);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email của bạn đã được xác thực thành công"
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("Verify email OTP error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Verify email OTP error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi xác thực email"
            ));
        }
    }
    
    /**
     * Kiểm tra thời gian cooldown còn lại để gửi OTP email
     */
    @GetMapping("/email/resend-cooldown")
    public ResponseEntity<?> getEmailOtpResendCooldown(@RequestParam String email) {
        try {
            if (!StringUtils.hasText(email)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email không được để trống"
                ));
            }
            
            long cooldownSeconds = emailOtpService.getResendCooldownSeconds(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cooldownSeconds", cooldownSeconds,
                "canResend", cooldownSeconds <= 0
            ));
            
        } catch (Exception e) {
            log.error("Get email OTP cooldown error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Không thể kiểm tra thời gian cooldown"
            ));
        }
    }

    /**
     * Gửi OTP cho quên mật khẩu qua email
     */
    @PostMapping("/email/forgot-password-request")
    public ResponseEntity<?> sendForgotPasswordOtp(@Valid @RequestBody ForgotPasswordEmailRequest request) {
        try {
            String email = request.email();
            
            // Gửi OTP cho quên mật khẩu
            emailOtpService.sendForgotPasswordOtp(email);
            
            long cooldownSeconds = emailOtpService.getResendCooldownSeconds(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mã OTP đã được gửi đến email của bạn",
                "expirationSeconds", 180, // 3 minutes
                "cooldownSeconds", cooldownSeconds
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("Send forgot password OTP error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Send forgot password OTP error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Không thể gửi mã OTP. Vui lòng thử lại sau"
            ));
        }
    }

    /**
     * Xác thực OTP và đặt lại mật khẩu
     */
    @PostMapping("/email/reset-password")
    public ResponseEntity<?> resetPasswordWithEmail(@Valid @RequestBody ResetPasswordEmailRequest request) {
        try {
            String email = request.email();
            String otp = request.otp();
            String newPassword = request.newPassword();
            
            // Xác thực OTP và đặt lại mật khẩu
            emailOtpService.verifyOtpAndResetPassword(email, otp, newPassword);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mật khẩu của bạn đã được đặt lại thành công"
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("Reset password with email error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Reset password with email error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi đặt lại mật khẩu"
            ));
        }
    }

    /**
     * Validate request gửi OTP dựa trên loại
     */
    private void validateSendOtpRequest(String phoneNumber, OtpType otpType) {
        String normalizedPhone = normalizePhoneNumber(phoneNumber);

        switch (otpType) {
            case REGISTER:
                // Kiểm tra số điện thoại chưa được đăng ký
                if (accountRepository.existsByPhoneNumber(normalizedPhone)) {
                    throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
                }
                break;

            case FORGOT_PASSWORD:
                // Kiểm tra số điện thoại đã tồn tại
                if (!accountRepository.existsByPhoneNumber(normalizedPhone)) {
                    throw new IllegalArgumentException("Số điện thoại chưa được đăng ký");
                }
                break;

            case VERIFY_PHONE:
            case CHANGE_PHONE:
                // Không cần validate thêm - dùng để xác thực số điện thoại mới
                break;
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String normalized = phoneNumber.trim().replaceAll("\\s+", "");
        if (normalized.startsWith("0")) {
            normalized = "+84" + normalized.substring(1);
        } else if (normalized.startsWith("84") && !normalized.startsWith("+84")) {
            normalized = "+" + normalized;
        } else if (!normalized.startsWith("+")) {
            normalized = "+84" + normalized;
        }
        return normalized;
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 6) {
            return "***";
        }
        return phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
}
