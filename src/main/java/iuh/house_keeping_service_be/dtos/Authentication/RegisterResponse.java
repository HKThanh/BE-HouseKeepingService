package iuh.house_keeping_service_be.dtos.Authentication;

public record RegisterResponse (
    String username,
    String email,
    String role,
    Boolean isEmailVerified,
    Boolean isPhoneVerified,
    String addressId
) {

}
