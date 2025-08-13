package iuh.house_keeping_service_be.services.AuthService;

import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;

public interface AuthService {
    String login(String username, String password, String role);

    Account register(String username, String password, String email, String role, String fullName);

    boolean validateToken(String token);

    String refreshToken(String token);

    String logout(String token);
}
