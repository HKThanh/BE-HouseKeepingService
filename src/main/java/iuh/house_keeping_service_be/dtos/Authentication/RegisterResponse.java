package iuh.house_keeping_service_be.dtos.Authentication;

public record RegisterResponse (
    String username,
    String email,
    String role
) {
    public RegisterResponse(String username, String email) {
        this(username, email, "USER");
    }

    public RegisterResponse(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
