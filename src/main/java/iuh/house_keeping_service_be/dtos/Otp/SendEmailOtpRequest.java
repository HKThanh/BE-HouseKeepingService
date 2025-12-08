package iuh.house_keeping_service_be.dtos.Otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request để gửi OTP đến email
 */
public record SendEmailOtpRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Loại OTP không được để trống")
        String otpType
) {
}
