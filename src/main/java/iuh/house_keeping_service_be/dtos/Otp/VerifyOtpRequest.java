package iuh.house_keeping_service_be.dtos.Otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request để xác thực OTP
 */
public record VerifyOtpRequest(
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(\\+84|84|0)?[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
        String phoneNumber,

        @NotBlank(message = "Mã OTP không được để trống")
        @Size(min = 6, max = 6, message = "Mã OTP phải có 6 chữ số")
        String otp,

        @NotBlank(message = "Loại OTP không được để trống")
        String otpType,

        // Session info từ Firebase (verificationId)
        String sessionInfo
) {
}
