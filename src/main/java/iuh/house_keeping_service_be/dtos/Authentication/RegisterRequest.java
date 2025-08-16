package iuh.house_keeping_service_be.dtos.Authentication;

public record RegisterRequest(
    String username,
    String password,
    String email,
    String phoneNumber,
    String role,
    String fullName
) {
}