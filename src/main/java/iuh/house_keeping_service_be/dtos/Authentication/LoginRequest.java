package iuh.house_keeping_service_be.dtos.Authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    String username,
    String password,
    String role,
    String deviceType
) {
}