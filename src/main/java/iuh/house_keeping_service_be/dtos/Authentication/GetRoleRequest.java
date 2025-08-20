package iuh.house_keeping_service_be.dtos.Authentication;

public record GetRoleRequest(
        String username,
        String password
) {
}
