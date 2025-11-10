package iuh.house_keeping_service_be.dtos.Authentication;

public record PhoneOtpVerifyResponse(
    String firebaseIdToken,
    String firebaseRefreshToken,
    String firebaseUid,
    String phoneNumber,
    String accountId,
    boolean phoneVerified,
    boolean newFirebaseUser,
    long expiresInSeconds
) {
}

