package iuh.house_keeping_service_be.dtos.Authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,

    @NotBlank(message = "Phone number is required")
    String phoneNumber,

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "CUSTOMER|EMPLOYEE|ADMIN", message = "Role must be CUSTOMER, EMPLOYEE, or ADMIN")
    String role,

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    String fullName
) {
}