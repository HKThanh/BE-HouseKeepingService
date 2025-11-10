package iuh.house_keeping_service_be.dtos.Authentication;

import jakarta.validation.constraints.NotBlank;

public record PhoneOtpRequest(
    @NotBlank(message = "Số điện thoại không được để trống")
    String phoneNumber,
    String recaptchaToken,
    String safetyNetToken
) {
}

