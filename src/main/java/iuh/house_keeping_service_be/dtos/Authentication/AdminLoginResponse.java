package iuh.house_keeping_service_be.dtos.Authentication;

public record AdminLoginResponse(
        String adminId,
        String accountId,
        String username,
        String fullName,
        boolean isMale,
        String department,
        String contactInfo,
        String hireDate
) {
}
