package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Authentication.LoginResponse;
import iuh.house_keeping_service_be.dtos.Authentication.RegisterResponse;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.services.AuthService.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            String role = loginRequest.get("role");

            String token = authService.login(username, password, role);

            // Return structured response with LoginResponse DTO
            LoginResponse response = new LoginResponse(
                token,
                username,
                loginRequest.get("email") != null ? loginRequest.get("email") : "",
                role
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "data", response
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Invalid credentials"
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        try {
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            String email = registerRequest.get("email");
            String role = registerRequest.get("role");
            String fullName = registerRequest.get("fullName");

            Account account = authService.register(username, password, email, role, fullName);

            // Return structured response with RegisterResponse DTO
            RegisterResponse response = new RegisterResponse(
                account.getUsername(),
                account.getUser().getEmail(),
                account.getRoles().name()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Registration successful",
                "data", response
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Registration failed"
            ));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Authorization header is required"
                ));
            }

            String token = authHeader.substring(7);
            String newToken = authService.refreshToken(token);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token refreshed successfully",
                "token", newToken
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Token refresh failed"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Authorization header is required"
                ));
            }

            String token = authHeader.substring(7);
            authService.logout(token);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful"
            ));

        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Logout failed"
            ));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Authorization header is required"
                ));
            }

            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token is valid",
                    "valid", true
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Token is invalid",
                    "valid", false
                ));
            }

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Token validation failed",
                "valid", false
            ));
        }
    }
}