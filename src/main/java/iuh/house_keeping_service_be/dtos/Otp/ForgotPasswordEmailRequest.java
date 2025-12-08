package iuh.house_keeping_service_be.dtos.Otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request để gửi OTP cho quên mật khẩu qua email
 */
public record ForgotPasswordEmailRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email
) {
}
