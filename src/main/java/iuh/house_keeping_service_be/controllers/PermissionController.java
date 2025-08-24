package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.request.UpdatePermissionRequest;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.*;
import iuh.house_keeping_service_be.services.AdminService.AdminService;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final AdminService adminService;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;

    @GetMapping("/roles")
    public ResponseEntity<RoleListResponse> getAllManageableRoles(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    new RoleListResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // Kiểm tra quyền admin
            if (adminService.isAdminByUsername(username)) {
                return ResponseEntity.status(403).body(
                    new RoleListResponse(false, "Chỉ quản trị viên mới có quyền truy cập", null)
                );
            }

            RoleListResponse response = permissionService.getAllManageableRoles();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting manageable roles: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                new RoleListResponse(false, "Lỗi hệ thống", null)
            );
        }
    }

    @GetMapping("/roles/{roleId}")
    public ResponseEntity<PermissionManagementResponse> getRolePermissions(
            @PathVariable Integer roleId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    new PermissionManagementResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // Kiểm tra quyền admin
            if (adminService.isAdminByUsername(username)) {
                return ResponseEntity.status(403).body(
                    new PermissionManagementResponse(false, "Chỉ quản trị viên mới có quyền truy cập", null)
                );
            }

            PermissionManagementResponse response = permissionService.getRolePermissions(roleId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting role permissions: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                new PermissionManagementResponse(false, "Lỗi hệ thống", null)
            );
        }
    }

    @PutMapping("/roles/{roleId}/features/{featureId}")
    public ResponseEntity<PermissionManagementResponse> updateRolePermission(
            @PathVariable Integer roleId,
            @PathVariable Integer featureId,
            @Valid @RequestBody UpdatePermissionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    new PermissionManagementResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // Kiểm tra quyền admin
            if (adminService.isAdminByUsername(username)) {
                return ResponseEntity.status(403).body(
                        new PermissionManagementResponse(false, "Chỉ quản trị viên mới có quyền truy cập", null)
                );
            }

            PermissionManagementResponse response = permissionService.updateRolePermission(
                roleId, featureId, request.isEnabled()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating role permission: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                new PermissionManagementResponse(false, "Lỗi hệ thống", null)
            );
        }
    }
}