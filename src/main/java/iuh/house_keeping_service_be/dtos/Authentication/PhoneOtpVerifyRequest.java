package iuh.house_keeping_service_be.dtos.Authentication;

import jakarta.validation.constraints.NotBlank;

public record PhoneOtpVerifyRequest(
    @NotBlank(message = "sessionInfo không được để trống")
    String sessionInfo,

    @NotBlank(message = "Mã OTP không được để trống")
    String otpCode
) {
}

