package iuh.house_keeping_service_be.dtos.Authentication;

public record TokenRefreshResponse(
    String access_token,
    String refresh_token
) {
}
