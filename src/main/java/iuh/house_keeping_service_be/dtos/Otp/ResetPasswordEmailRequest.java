package iuh.house_keeping_service_be.dtos.Otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request để xác thực OTP email và đặt lại mật khẩu
 */
public record ResetPasswordEmailRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mã OTP không được để trống")
        @Size(min = 6, max = 6, message = "Mã OTP phải có 6 chữ số")
        String otp,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
        String newPassword
) {
}
