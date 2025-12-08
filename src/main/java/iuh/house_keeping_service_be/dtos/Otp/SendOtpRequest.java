package iuh.house_keeping_service_be.dtos.Otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request để gửi OTP đến số điện thoại
 */
public record SendOtpRequest(
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(\\+84|84|0)?[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
        String phoneNumber,

        @NotBlank(message = "Loại OTP không được để trống")
        String otpType,

        // Tùy chọn: recaptcha token để bảo vệ chống spam
        String recaptchaToken
) {
}
