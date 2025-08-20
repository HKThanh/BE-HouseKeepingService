package iuh.house_keeping_service_be.services.AuthService;

import iuh.house_keeping_service_be.dtos.Authentication.TokenPair;
import iuh.house_keeping_service_be.models.Account;

public interface AuthService {
    TokenPair login(String username, String password, String role, String deviceType);

    Account register(String username, String password, String email, String phoneNumber, String role, String fullName);

    boolean validateToken(String token);

    TokenPair refreshToken(String token);

    String logout(String token);

    String logoutAllDevices(String username);

    void updateLastLoginTime(Account account);

    void changePassword(String username, String currentPassword, String newPassword);

    String getRole(String username, String password);
}
