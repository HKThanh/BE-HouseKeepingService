package iuh.house_keeping_service_be.dtos.Authentication;

public record LoginResponse (
    String access_token,
    String refresh_token,
    String username,
    String email,
    String role
) {
}
