package iuh.house_keeping_service_be.dtos.Authentication;

public record LoginFailResponse (
    boolean success,
    String message
){}