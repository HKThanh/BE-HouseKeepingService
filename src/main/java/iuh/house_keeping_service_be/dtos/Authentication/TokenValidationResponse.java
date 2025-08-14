package iuh.house_keeping_service_be.dtos.Authentication;

public record TokenValidationResponse(
        boolean valid,
        String message,
        String username,
        String role
) {
}
