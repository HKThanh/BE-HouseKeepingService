package iuh.house_keeping_service_be.dtos.Authentication;

public record AdminLoginResponse(
        String adminId,
        String username,
        String fullName,
        boolean isMale,
        String address,
        String department,
        String contactInfo,
        String hireDate
) {
}
