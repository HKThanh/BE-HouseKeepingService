package iuh.house_keeping_service_be.services.OtpService;

import iuh.house_keeping_service_be.enums.OtpType;

/**
 * Service interface cho OTP operations
 * Sử dụng Firebase Authentication để gửi và xác thực OTP
 */
public interface OtpService {
    
    /**
     * Gửi OTP đến số điện thoại qua Firebase
     * @param phoneNumber Số điện thoại nhận OTP
     * @param otpType Loại OTP (REGISTER, FORGOT_PASSWORD, VERIFY_PHONE)
     * @param recaptchaToken Token reCAPTCHA (optional, for web clients)
     * @return Session info (verificationId) để sử dụng khi verify
     */
    String sendOtp(String phoneNumber, OtpType otpType, String recaptchaToken);
    
    /**
     * Xác minh OTP
     * @param phoneNumber Số điện thoại
     * @param otp Mã OTP 6 số
     * @param otpType Loại OTP
     * @param sessionInfo Session info từ sendOtp (verificationId)
     * @return Token xác thực nếu OTP hợp lệ
     */
    String verifyOtp(String phoneNumber, String otp, OtpType otpType, String sessionInfo);
    
    /**
     * Kiểm tra token xác thực OTP có hợp lệ không
     * @param phoneNumber Số điện thoại
     * @param token Token xác thực
     * @param otpType Loại OTP
     * @return true nếu hợp lệ
     */
    boolean validateVerificationToken(String phoneNumber, String token, OtpType otpType);
    
    /**
     * Xóa token xác thực sau khi sử dụng
     * @param phoneNumber Số điện thoại
     * @param otpType Loại OTP
     */
    void invalidateVerificationToken(String phoneNumber, OtpType otpType);
    
    /**
     * Lấy thời gian chờ gửi lại OTP (giây)
     * @param phoneNumber Số điện thoại
     * @param otpType Loại OTP
     * @return Số giây còn phải chờ, 0 nếu có thể gửi ngay
     */
    long getResendCooldown(String phoneNumber, OtpType otpType);
    
    /**
     * Kiểm tra Firebase OTP service có được bật không
     */
    boolean isEnabled();
}
