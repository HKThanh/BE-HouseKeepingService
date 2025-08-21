package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.services.CustomerService.CustomerService;
import iuh.house_keeping_service_be.services.AuthorizationService.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer")
@Slf4j
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomerById(@PathVariable String customerId,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            // Check if user can access this customer resource
            if (!authorizationService.canAccessResource(authHeader, customerId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied. You can only access your own data."
                ));
            }

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
}