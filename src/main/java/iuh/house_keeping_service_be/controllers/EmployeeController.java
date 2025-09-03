package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.EmployeeService.EmployeeService;
import iuh.house_keeping_service_be.services.AuthorizationService.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee")
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

//    private final AuthorizationService authorizationService;

    private final PermissionService permissionService;

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getEmployeeById(@PathVariable String employeeId,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            var employee = employeeService.findById(employeeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", employee
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

    @GetMapping("/{employeeId}/features")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getEnabledFeaturesByRoleEmployee(@PathVariable String employeeId,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            List<String> features = permissionService.getEnabledPermissionsByRole(RoleName.EMPLOYEE);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", features
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
}