package iuh.house_keeping_service_be.dtos.Authentication;

public record CustomerLoginResponse(
        String customerId,
        String accountId,
        String username,
        String avatar,
        String fullName,
        String email,
        String phoneNumber,
        boolean isMale,
        String status,
        String address
) {
}
