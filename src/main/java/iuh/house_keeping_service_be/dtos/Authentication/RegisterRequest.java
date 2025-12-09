package iuh.house_keeping_service_be.dtos.Authentication;

import jakarta.validation.Valid;

public record RegisterRequest(
    String username,
    String password,
    String email,
    String phoneNumber,
    String role,
    String fullName,
    @Valid
    RegisterAddressRequest address
) {
}