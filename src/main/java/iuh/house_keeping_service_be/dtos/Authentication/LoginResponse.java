package iuh.house_keeping_service_be.dtos.Authentication;

public record LoginResponse (
    String token,
    String username,
    String email,
    String role
) {
    public LoginResponse(String token, String username, String email) {
        this(token, username, email, "USER");
    }

    public LoginResponse(String token, String username, String email, String role) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
