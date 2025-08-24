package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.dtos.Service.ServiceListResponse;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.ServiceService.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/services")
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceController {

    private final ServiceService serviceService;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ServiceListResponse> getAllServices(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new ServiceListResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username;

            try {
                username = jwtUtil.extractUsername(token);
                if (username == null || !jwtUtil.validateToken(token, username)) {
                    return ResponseEntity.badRequest().body(
                            new ServiceListResponse(false, "Token không hợp lệ", null)
                    );
                }
            } catch (Exception e) {
                // Token malformed, expired, hoặc invalid
                return ResponseEntity.badRequest().body(
                        new ServiceListResponse(false, "Token không hợp lệ", null)
                );
            }

            // Kiểm tra quyền xem dịch vụ
            if (!permissionService.hasPermission(username, "service.view")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ServiceListResponse(false, "Không có quyền xem danh sách dịch vụ", null)
                );
            }

            ServiceListResponse response = serviceService.getAllActiveServices();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting services for customer: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ServiceListResponse(false, "Lỗi hệ thống", null)
            );
        }
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceDetailResponse> getServiceDetail(
            @PathVariable Integer serviceId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new ServiceDetailResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // Kiểm tra quyền xem chi tiết dịch vụ
            if (!permissionService.hasPermission(username, "service.view")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ServiceDetailResponse(false, "Không có quyền xem chi tiết dịch vụ", null)
                );
            }

            ServiceDetailResponse response = serviceService.getServiceDetail(serviceId);

            if (!response.success()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting service detail {} for customer: {}", serviceId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ServiceDetailResponse(false, "Lỗi hệ thống", null)
            );
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ServiceListResponse> searchServices(
            @RequestParam(required = false) String keyword,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new ServiceListResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // Kiểm tra quyền tìm kiếm dịch vụ
            if (!permissionService.hasPermission(username, "service.view")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ServiceListResponse(false, "Không có quyền tìm kiếm dịch vụ", null)
                );
            }

            ServiceListResponse response = serviceService.searchServices(keyword);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error searching services with keyword '{}' for customer: {}", keyword, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ServiceListResponse(false, "Lỗi hệ thống", null)
            );
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getServiceCount(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Token không hợp lệ")
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // Kiểm tra quyền xem thống kê dịch vụ
            if (!permissionService.hasPermission(username, "service.view")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("success", false, "message", "Không có quyền xem thống kê dịch vụ")
                );
            }

            Long count = serviceService.countActiveServices();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy số lượng dịch vụ thành công",
                    "data", Map.of("totalServices", count)
            ));

        } catch (Exception e) {
            log.error("Error getting service count for customer: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    Map.of("success", false, "message", "Lỗi hệ thống")
            );
        }
    }
}