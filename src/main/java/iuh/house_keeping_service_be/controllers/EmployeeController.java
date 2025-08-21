package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.services.EmployeeService.EmployeeService;
import iuh.house_keeping_service_be.services.AuthorizationService.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String employeeId,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            // Check if user can access this employee resource
            if (!authorizationService.canAccessResource(authHeader, employeeId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. You can only access your own data."
                ));
            }

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
}