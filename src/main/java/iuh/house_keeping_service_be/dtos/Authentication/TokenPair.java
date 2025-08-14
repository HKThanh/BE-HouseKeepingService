package iuh.house_keeping_service_be.dtos.Authentication;

public record TokenPair(
    String accessToken,
    String refreshToken
) {
}
