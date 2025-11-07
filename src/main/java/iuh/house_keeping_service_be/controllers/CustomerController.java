package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.PermissionManagementResponse;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.UserPermissionData;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.UserPermissionsResponse;
import iuh.house_keeping_service_be.dtos.Cloudinary.CloudinaryUploadResult;
import iuh.house_keeping_service_be.dtos.Customer.CustomerUpdateRequest;
import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.CloudinaryService.CloudinaryService;
import iuh.house_keeping_service_be.services.CustomerService.CustomerService;
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
@RequestMapping("/api/v1/customer")
@Slf4j
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    private final AuthorizationService authorizationService;

    private final PermissionService permissionService;

    private final CloudinaryService cloudinaryService;

    private final JwtUtil jwtUtil;

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')")
    public ResponseEntity<?> getCustomerById(@PathVariable String customerId,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            var customerProfile = customerService.getCustomerProfile(customerId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", customerProfile
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

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getActiveCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, sort[0])));
        var customersPage = customerService.getActiveCustomers(pageable);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", customersPage.getContent(),
                "currentPage", customersPage.getNumber(),
                "totalItems", customersPage.getTotalElements(),
                "totalPages", customersPage.getTotalPages()
        ));
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

            Account customerAccount = customer.getAccount();

//            UserPermissionsResponse userPermissionsResponse = permissionService.getUserPermissions(customer.getAccount().getUsername());

            PermissionManagementResponse userPermissionsResponse = permissionService.getRolePermissions(customerAccount.getRoles().stream()
                .filter(role -> role.getRoleName().equals(RoleName.CUSTOMER))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không có vai trò hợp lệ"))
                .getRoleId()
            );
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

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')")
    public ResponseEntity<?> updateCustomer(@PathVariable String customerId,
                                            @RequestBody CustomerUpdateRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            if (!authorizationService.canAccessResource(authHeader, customerId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. You can only update your own data."
                ));
            }

            Customer updatedCustomer = customerService.updateCustomer(customerId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updatedCustomer
            ));
        } catch (IllegalArgumentException e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating customer: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi cập nhật thông tin khách hàng"
            ));
        }
    }

    @PutMapping("/{customerId}/deactivate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deactivateCustomer(@PathVariable String customerId) {
        try {
            Customer deactivatedCustomer = customerService.inActivateCustomer(customerId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", deactivatedCustomer
            ));
        } catch (IllegalArgumentException e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deactivating customer: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi vô hiệu hóa khách hàng"
            ));
        }
    }

    @PostMapping("/{customerId}/avatar")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')")
    public ResponseEntity<?> uploadAvatar(@PathVariable String customerId,
                                          @RequestParam("avatar") MultipartFile avatar,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            if (!authorizationService.canAccessResource(authHeader, customerId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. You can only update your own data."
                ));
            }

            CloudinaryUploadResult uploadResult = cloudinaryService.uploadCustomerAvatar(avatar);
            Customer updatedCustomer = customerService.updateAvatar(customerId, uploadResult.secureUrl());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "customer", updatedCustomer,
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