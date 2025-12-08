package iuh.house_keeping_service_be.services.EmailOtpService.impl;

import iuh.house_keeping_service_be.enums.OtpType;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.services.EmailOtpService.EmailOtpService;
import iuh.house_keeping_service_be.services.EmailService.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOtpServiceImpl implements EmailOtpService {

    private static final String EMAIL_OTP_KEY_PREFIX = "email_otp:";
    private static final String EMAIL_OTP_COOLDOWN_PREFIX = "email_otp_cooldown:";
    private static final String EMAIL_OTP_ATTEMPTS_PREFIX = "email_otp_attempts:";
    
    private static final int OTP_VALIDITY_MINUTES = 3;
    private static final int COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;
    private static final int OTP_LENGTH = 6;
    
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public boolean sendEmailOtp(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        
        email = email.trim().toLowerCase();
        
        // Kiểm tra cooldown
        String cooldownKey = EMAIL_OTP_COOLDOWN_PREFIX + email;
        Long cooldownTtl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        if (cooldownTtl != null && cooldownTtl > 0) {
            throw new IllegalArgumentException("Vui lòng chờ " + cooldownTtl + " giây trước khi gửi lại OTP");
        }
        
        // Sinh OTP 6 số
        String otp = generateOtp();
        
        // Lưu OTP vào Redis với TTL 3 phút
        String otpKey = EMAIL_OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(otpKey, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        
        // Lưu cooldown vào Redis với TTL 60 giây
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);
        
        // Reset attempts counter
        String attemptsKey = EMAIL_OTP_ATTEMPTS_PREFIX + email;
        redisTemplate.delete(attemptsKey);
        
        // Gửi OTP qua email
        try {
            String subject = "Mã xác thực email của bạn - House Keeping Service";
            String htmlContent = buildEmailOtpTemplate(otp);
            emailService.sendEmail(email, subject, htmlContent);
            log.info("Email OTP sent to {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email OTP to {}", email, e);
            // Xóa OTP nếu gửi email thất bại
            redisTemplate.delete(otpKey);
            redisTemplate.delete(cooldownKey);
            throw new RuntimeException("Gửi OTP thất bại, vui lòng thử lại sau");
        }
    }

    @Override
    public boolean verifyEmailOtp(String email, String otp) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(otp)) {
            throw new IllegalArgumentException("Email và OTP không được để trống");
        }
        
        email = email.trim().toLowerCase();
        otp = otp.trim();
        
        // Kiểm tra số lần nhập sai
        String attemptsKey = EMAIL_OTP_ATTEMPTS_PREFIX + email;
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = 0;
        if (StringUtils.hasText(attemptsStr)) {
            try {
                attempts = Integer.parseInt(attemptsStr);
            } catch (NumberFormatException e) {
                attempts = 0;
            }
        }
        
        if (attempts >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException("Bạn đã nhập sai OTP quá nhiều lần, vui lòng yêu cầu OTP mới");
        }
        
        // Lấy OTP từ Redis
        String otpKey = EMAIL_OTP_KEY_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        
        if (!StringUtils.hasText(storedOtp)) {
            throw new IllegalArgumentException("OTP đã hết hạn hoặc chưa được gửi");
        }
        
        // Kiểm tra OTP
        if (!storedOtp.equals(otp)) {
            attempts++;
            redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts), 
                    OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
            throw new IllegalArgumentException("OTP không chính xác (" + (MAX_ATTEMPTS - attempts) + " lần thử còn lại)");
        }
        
        // OTP đúng, cập nhật isEmailVerified cho Customer hoặc Employee
        updateEmailVerificationStatus(email);
        
        // Xóa OTP và cooldown sau khi verify thành công
        clearEmailOtp(email);
        redisTemplate.delete(attemptsKey);
        
        log.info("Email verified successfully for: {}", email);
        return true;
    }

    @Override
    public long getResendCooldownSeconds(String email) {
        if (!StringUtils.hasText(email)) {
            return 0;
        }
        
        email = email.trim().toLowerCase();
        String cooldownKey = EMAIL_OTP_COOLDOWN_PREFIX + email;
        Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    @Override
    public boolean isEmailVerified(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        
        email = email.trim().toLowerCase();
        
        // Kiểm tra trong Customer
        var customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (customer.getIsEmailVerified() != null && customer.getIsEmailVerified()) {
                return true;
            }
        }
        
        // Kiểm tra trong Employee
        var employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            if (employee.getIsEmailVerified() != null && employee.getIsEmailVerified()) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void clearEmailOtp(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        
        email = email.trim().toLowerCase();
        String otpKey = EMAIL_OTP_KEY_PREFIX + email;
        redisTemplate.delete(otpKey);
    }

    /**
     * Sinh OTP 6 số ngẫu nhiên
     */
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Cập nhật trạng thái xác thực email cho Customer hoặc Employee
     */
    private void updateEmailVerificationStatus(String email) {
        email = email.trim().toLowerCase();
        
        // Cố gắng cập nhật Customer trước
        var customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setIsEmailVerified(true);
            customerRepository.save(customer);
            return;
        }
        
        // Nếu không tìm thấy Customer, cập nhật Employee
        var employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setIsEmailVerified(true);
            employeeRepository.save(employee);
            return;
        }
        
        // Không tìm thấy email nào
        throw new IllegalArgumentException("Email không tồn tại trong hệ thống");
    }

    /**
     * Tạo template HTML cho email chứa OTP
     */
    private String buildEmailOtpTemplate(String otp) {
        return "<!DOCTYPE html>" +
                "<html lang=\"vi\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Mã xác thực email</title>" +
                "</head>" +
                "<body style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5;\">" +
                "    <div style=\"max-width: 600px; margin: 20px auto; background-color: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden;\">" +
                "        <!-- Header -->" +
                "        <div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px 20px; text-align: center; color: white;\">" +
                "            <h1 style=\"margin: 0; font-size: 24px; font-weight: 600;\">House Keeping Service</h1>" +
                "            <p style=\"margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;\">Xác thực email của bạn</p>" +
                "        </div>" +
                "        " +
                "        <!-- Content -->" +
                "        <div style=\"padding: 40px 20px; text-align: center;\">" +
                "            <h2 style=\"color: #333; margin: 0 0 10px 0; font-size: 20px;\">Mã xác thực của bạn</h2>" +
                "            <p style=\"color: #666; margin: 0 0 30px 0; font-size: 14px;\">Vui lòng nhập mã dưới đây để xác thực email của bạn</p>" +
                "            " +
                "            <!-- OTP Code -->" +
                "            <div style=\"background-color: #f0f0f0; border: 2px dashed #667eea; border-radius: 8px; padding: 30px; margin: 30px 0;\">" +
                "                <p style=\"margin: 0; font-size: 48px; font-weight: bold; color: #667eea; letter-spacing: 8px;\">" + otp + "</p>" +
                "            </div>" +
                "            " +
                "            <p style=\"color: #999; margin: 20px 0; font-size: 13px;\">Mã này sẽ hết hạn trong 3 phút</p>" +
                "        </div>" +
                "        " +
                "        <!-- Footer -->" +
                "        <div style=\"background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eee;\">" +
                "            <p style=\"margin: 0; color: #999; font-size: 12px;\">" +
                "                Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này." +
                "            </p>" +
                "            <p style=\"margin: 10px 0 0 0; color: #999; font-size: 12px;\">" +
                "                &copy; 2025 House Keeping Service. All rights reserved." +
                "            </p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
