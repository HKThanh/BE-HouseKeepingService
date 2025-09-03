package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.UserPermissionData;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.UserPermissionsResponse;
import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.CustomerService.CustomerService;
import iuh.house_keeping_service_be.services.AuthorizationService.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer")
@Slf4j
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

//    private final AuthorizationService authorizationService;

    private final PermissionService permissionService;

    private final JwtUtil jwtUtil;

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')")
    public ResponseEntity<?> getCustomerById(@PathVariable String customerId,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            var customer = customerService.findById(customerId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", customer
            ));
        } catch (IllegalArgumentException e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching customer profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy thông tin khách hàng"
            ));
        }
    }

    @GetMapping("/{customerId}/features")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')")
    public ResponseEntity<?> getCustomerFeatures(@PathVariable String customerId,
                                                 @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new ServiceDetailResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            Customer customer = customerService.findById(customerId);

            if (customer == null) {
                return ResponseEntity.badRequest().body(
                    Map.of(
                        "success", false,
                        "message", "Khách hàng không tồn tại"
                    )
                );
            }

            UserPermissionsResponse userPermissionsResponse = permissionService.getUserPermissions(customer.getAccount().getUsername());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", userPermissionsResponse
            ));
        } catch (IllegalArgumentException e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching customer features: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy danh sách tính năng của khách hàng"
            ));
        }
    }
}