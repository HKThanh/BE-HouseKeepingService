package iuh.house_keeping_service_be.dtos.Otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request để đặt lại mật khẩu sau khi xác thực OTP
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(\\+84|84|0)?[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
        String phoneNumber,

        @NotBlank(message = "Token xác thực không được để trống")
        String verificationToken,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
        String newPassword,

        @NotBlank(message = "Vai trò không được để trống")
        String role
) {
}
