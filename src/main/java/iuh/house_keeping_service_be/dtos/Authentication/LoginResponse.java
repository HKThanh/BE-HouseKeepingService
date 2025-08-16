package iuh.house_keeping_service_be.dtos.Authentication;

public record LoginResponse (
        String access_Token,
        String refresh_Token,
        int expire_In,
        String role,
        EmployeeLoginResponse data
) {
}
