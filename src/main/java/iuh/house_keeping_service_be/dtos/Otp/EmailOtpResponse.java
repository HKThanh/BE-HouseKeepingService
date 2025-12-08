package iuh.house_keeping_service_be.dtos.Otp;

import lombok.Builder;

/**
 * Response cho các API Email OTP
 */
@Builder
public record EmailOtpResponse(
        boolean success,
        String message,
        Integer expirationSeconds,
        Long cooldownSeconds
) {
    // Constructor cho response đơn giản
    public EmailOtpResponse(boolean success, String message) {
        this(success, message, null, null);
    }
}
