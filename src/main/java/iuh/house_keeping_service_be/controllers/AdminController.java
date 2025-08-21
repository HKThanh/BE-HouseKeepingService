package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.services.AdminService.AdminService;
import iuh.house_keeping_service_be.services.AuthorizationService.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/{adminId}")
    public ResponseEntity<?> getAdminById(@PathVariable String adminId,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            // Check if user can access this admin resource
            if (!authorizationService.canAccessResource(authHeader, adminId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. You can only access your own data."
                ));
            }

            var admin = adminService.findById(adminId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", admin
            ));
        } catch (IllegalArgumentException e) {
            log.error("Admin not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching admin profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy thông tin quản trị viên"
            ));
        }
    }
}