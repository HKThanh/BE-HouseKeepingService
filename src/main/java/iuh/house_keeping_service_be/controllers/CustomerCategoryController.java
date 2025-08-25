package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.ServiceCategory.CategoryWithServicesResponse;
import iuh.house_keeping_service_be.dtos.ServiceCategory.ServiceCategoryListResponse;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.ServiceCategoryService.ServiceCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/categories")
@RequiredArgsConstructor
@Slf4j
public class CustomerCategoryController {

    private final ServiceCategoryService serviceCategoryService;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ServiceCategoryListResponse> getAllCategories(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token
            ResponseEntity<ServiceCategoryListResponse> tokenValidation = validateToken(authHeader);
            if (tokenValidation != null) {
                return tokenValidation;
            }

            String username = jwtUtil.extractUsername(authHeader.substring(7));

            // Check permissions
            if (!permissionService.hasPermission(username, "service.view")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ServiceCategoryListResponse(false, "Không có quyền xem danh mục dịch vụ", null)
                );
            }

            ServiceCategoryListResponse response = serviceCategoryService.getAllActiveCategories();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting categories for customer: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ServiceCategoryListResponse(false, "Lỗi hệ thống", null)
            );
        }
    }

    @GetMapping("/{categoryId}/services")
    public ResponseEntity<CategoryWithServicesResponse> getCategoryWithServices(
            @PathVariable Integer categoryId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token
            ResponseEntity<CategoryWithServicesResponse> tokenValidation = validateTokenForCategory(authHeader);
            if (tokenValidation != null) {
                return tokenValidation;
            }

            String username = jwtUtil.extractUsername(authHeader.substring(7));

            // Check permissions
            if (!permissionService.hasPermission(username, "service.view")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new CategoryWithServicesResponse(false, "Không có quyền xem dịch vụ trong danh mục", null)
                );
            }

            CategoryWithServicesResponse response = serviceCategoryService.getCategoryWithServices(categoryId);

            if (!response.success()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting category {} with services for customer: {}", categoryId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new CategoryWithServicesResponse(false, "Lỗi hệ thống", null)
            );
        }
    }

    @GetMapping("/{categoryId}/count")
    public ResponseEntity<Map<String, Object>> getServiceCountByCategory(
            @PathVariable Integer categoryId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }

            String token = authHeader.substring(7);
            String username;

            try {
                username = jwtUtil.extractUsername(token);
                if (username == null || !jwtUtil.validateToken(token, username)) {
                    return ResponseEntity.badRequest().body(
                            Map.of("success", false, "message", "Token không hợp lệ")
                    );
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }

            // Check permissions
            if (!permissionService.hasPermission(username, "service.view")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("success", false, "message", "Không có quyền xem thống kê dịch vụ")
                );
            }

            Long count = serviceCategoryService.countServicesByCategory(categoryId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy số lượng dịch vụ trong danh mục thành công",
                    "data", Map.of("categoryId", categoryId, "serviceCount", count)
            ));

        } catch (Exception e) {
            log.error("Error getting service count for category {} for customer: {}", categoryId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    Map.of("success", false, "message", "Lỗi hệ thống")
            );
        }
    }

    private ResponseEntity<ServiceCategoryListResponse> validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    new ServiceCategoryListResponse(false, "Token không hợp lệ", null)
            );
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.badRequest().body(
                        new ServiceCategoryListResponse(false, "Token không hợp lệ", null)
                );
            }
            return null; // Valid token
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ServiceCategoryListResponse(false, "Token không hợp lệ", null)
            );
        }
    }

    private ResponseEntity<CategoryWithServicesResponse> validateTokenForCategory(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    new CategoryWithServicesResponse(false, "Token không hợp lệ", null)
            );
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.badRequest().body(
                        new CategoryWithServicesResponse(false, "Token không hợp lệ", null)
                );
            }
            return null; // Valid token
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CategoryWithServicesResponse(false, "Token không hợp lệ", null)
            );
        }
    }
}