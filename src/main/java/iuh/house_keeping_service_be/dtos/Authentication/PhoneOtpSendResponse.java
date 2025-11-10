package iuh.house_keeping_service_be.dtos.Authentication;

public record PhoneOtpSendResponse(
    String sessionInfo,
    long expiresInSeconds
) {
}

