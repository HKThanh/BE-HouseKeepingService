package iuh.house_keeping_service_be.services.EmailOtpService;

/**
 * Service interface cho Email OTP operations
 * Sử dụng Redis để lưu trữ OTP và gửi OTP qua Email
 */
public interface EmailOtpService {
    
    /**
     * Gửi OTP đến email
     * OTP sẽ có hiệu lực trong 3 phút, cooldown 60 giây giữa các lần gửi
     * @param email Email nhận OTP
     * @return true nếu gửi thành công
     * @throws IllegalArgumentException nếu email không hợp lệ hoặc đang trong cooldown
     */
    boolean sendEmailOtp(String email);
    
    /**
     * Xác thực OTP email
     * @param email Email
     * @param otp Mã OTP 6 số
     * @return true nếu OTP hợp lệ và cập nhật isEmailVerified thành công
     * @throws IllegalArgumentException nếu OTP không đúng hoặc hết hạn
     */
    boolean verifyEmailOtp(String email, String otp);
    
    /**
     * Kiểm tra cooldown còn lại (giây) từ lần gửi OTP cuối cùng
     * @param email Email
     * @return Số giây còn lại, 0 nếu hết cooldown hoặc chưa gửi OTP bao giờ
     */
    long getResendCooldownSeconds(String email);
    
    /**
     * Kiểm tra email đã được xác thực chưa
     * @param email Email
     * @return true nếu email đã xác thực
     */
    boolean isEmailVerified(String email);
    
    /**
     * Xóa OTP và cooldown khi hết hạn hoặc sử dụng xong
     * @param email Email
     */
    void clearEmailOtp(String email);
}
