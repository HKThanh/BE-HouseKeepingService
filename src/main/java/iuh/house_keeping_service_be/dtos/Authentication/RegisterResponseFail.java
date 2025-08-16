package iuh.house_keeping_service_be.dtos.Authentication;

public record RegisterResponseFail(
        boolean success,
        String message,
        String field
) {
}
