package iuh.house_keeping_service_be.dtos.Authentication;

public record DataLoginResponse(
        String username,
        String avatar,
        String full_name,
        String email,
        String phone_number,
        boolean is_male,
        String status,
        String address
) {
}
