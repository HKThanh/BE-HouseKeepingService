package iuh.house_keeping_service_be.dtos.Otp;

import lombok.Builder;

/**
 * Response cho các API OTP
 */
@Builder
public record OtpResponse(
        boolean success,
        String message,
        String sessionInfo,      // Firebase verificationId (để verify OTP)
        String verificationToken, // Token sau khi verify thành công (để reset password, etc.)
        Integer expirationSeconds,
        Long resendAfterSeconds
) {
    // Constructor cho response đơn giản
    public OtpResponse(boolean success, String message) {
        this(success, message, null, null, null, null);
    }
}
