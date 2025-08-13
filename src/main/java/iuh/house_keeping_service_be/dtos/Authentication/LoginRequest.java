package iuh.house_keeping_service_be.dtos.Authentication;

public record LoginRequest (
    String username,
    String password,
    String role
) {
    public LoginRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or blank");
        }
    }
}