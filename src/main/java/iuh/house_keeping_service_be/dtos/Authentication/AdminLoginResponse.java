package iuh.house_keeping_service_be.dtos.Authentication;

public record AdminLoginResponse(
        String username,
        String full_name,
        boolean is_male,
        String address,
        String department,
        String contact_info,
        String hire_date
) {
}
