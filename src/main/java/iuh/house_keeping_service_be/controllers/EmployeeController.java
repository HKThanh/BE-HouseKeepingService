package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.PermissionManagementResponse;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.UserPermissionsResponse;
import iuh.house_keeping_service_be.dtos.Cloudinary.CloudinaryUploadResult;
import iuh.house_keeping_service_be.dtos.Employee.UpdateEmployeeRequest;
import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.CloudinaryService.CloudinaryService;
import iuh.house_keeping_service_be.services.EmployeeService.EmployeeService;
import iuh.house_keeping_service_be.services.AuthorizationService.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee")
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    private final AuthorizationService authorizationService;

    private final PermissionService permissionService;

    private final CloudinaryService cloudinaryService;

    private final JwtUtil jwtUtil;

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getEmployeeById(@PathVariable String employeeId,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            var employeeProfile = employeeService.getEmployeeProfile(employeeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", employeeProfile
            ));
        } catch (IllegalArgumentException e) {
            log.error("Employee not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching employee profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy thông tin nhân viên"
            ));
        }
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> updateEmployee(@PathVariable String employeeId,
                                            @RequestBody UpdateEmployeeRequest request) {
        try {
            Employee updated = employeeService.updateEmployee(employeeId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating employee: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating employee: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi cập nhật thông tin nhân viên"
            ));
        }
    }

    @GetMapping("/{employeeId}/features")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getEnabledFeaturesByRoleEmployee(@PathVariable String employeeId,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new ServiceDetailResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            Employee employee = employeeService.findById(employeeId);

            if (employee == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Nhân viên không tồn tại"
                ));
            }

            Account employeeAccount = employee.getAccount();

//            UserPermissionsResponse userPermissionsResponse = permissionService.getUserPermissions(employeeAccount.getUsername());


            PermissionManagementResponse userPermissionsResponse = permissionService.getRolePermissions(employeeAccount.getRoles().stream()
                    .filter(role -> role.getRoleName() != RoleName.CUSTOMER)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Nhân viên không có vai trò hợp lệ"))
                    .getRoleId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", userPermissionsResponse
            ));
        } catch (IllegalArgumentException e) {
            log.error("Employee not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching employee profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy thông tin nhân viên"
            ));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        try {
            Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort.Order order = new Sort.Order(direction, sort[0]);

            Pageable pageable = PageRequest.of(page, size, Sort.by(order));
            var employeesPage = employeeService.getAllEmployees(pageable);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", employeesPage.getContent(),
                    "currentPage", employeesPage.getNumber(),
                    "totalItems", employeesPage.getTotalElements(),
                    "totalPages", employeesPage.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching employees: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy danh sách nhân viên"
            ));
        }
    }

    @PostMapping("/{employeeId}/avatar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> uploadAvatar(@PathVariable String employeeId,
                                          @RequestParam("avatar") MultipartFile avatar,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            if (!authorizationService.canAccessResource(authHeader, employeeId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. You can only update your own data."
                ));
            }

            CloudinaryUploadResult uploadResult = cloudinaryService.uploadEmployeeAvatar(avatar);
            Employee updatedEmployee = employeeService.updateAvatar(employeeId, uploadResult.secureUrl());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "employee", updatedEmployee,
                            "avatarPublicId", uploadResult.publicId()
                    )
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid avatar upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (RuntimeException e) {
            log.error("Error uploading avatar: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi tải ảnh đại diện"
            ));
        }
    }


}