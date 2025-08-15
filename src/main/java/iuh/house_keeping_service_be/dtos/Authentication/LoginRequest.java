package iuh.house_keeping_service_be.dtos.Authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    String username,
    String password,

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "CUSTOMER|EMPLOYEE|ADMIN", message = "Role must be CUSTOMER, EMPLOYEE, or ADMIN")
    String role
) {
}