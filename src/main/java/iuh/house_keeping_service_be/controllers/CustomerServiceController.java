package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.ApiResponse;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeRequest;
import iuh.house_keeping_service_be.dtos.EmployeeSchedule.SuitableEmployeeResponse;
import iuh.house_keeping_service_be.dtos.Service.*;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.EmployeeScheduleService.EmployeeScheduleService;
import iuh.house_keeping_service_be.services.RecommendationService.EmployeeRecommendationService;
import iuh.house_keeping_service_be.services.ServiceService.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/services")
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceController {

    private final ServiceService serviceService;
    private final PermissionService permissionService;
    private final EmployeeScheduleService employeeScheduleService;
    private final EmployeeRecommendationService employeeRecommendationService;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

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

    @GetMapping("/{serviceId}/options")
    public ResponseEntity<ServiceOptionsResponse> getServiceOptions(
            @PathVariable Integer serviceId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new ServiceOptionsResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.badRequest().body(
                        new ServiceOptionsResponse(false, "Token không hợp lệ", null)
                );
            }

            ServiceOptionsResponse response = serviceService.getServiceOptions(serviceId);

            if (!response.success()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting service options for service {}: {}", serviceId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ServiceOptionsResponse(false, "Lỗi hệ thống", null)
            );
        }
    }

    @PostMapping("/calculate-price")
    public ResponseEntity<CalculatePriceResponse> calculatePrice(
            @RequestBody CalculatePriceRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new CalculatePriceResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.badRequest().body(
                        new CalculatePriceResponse(false, "Token không hợp lệ", null)
                );
            }

            CalculatePriceResponse response = serviceService.calculatePrice(request);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("Error in calculatePrice endpoint: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CalculatePriceResponse(
                            false,
                            "Lỗi hệ thống khi tính toán giá",
                            null
                    ));
        }
    }

    @GetMapping("/employee/suitable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<List<SuitableEmployeeResponse>>> findSuitableEmployees(
            @RequestParam Integer serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bookingTime,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) List<LocalDateTime> bookingTimes) {

        if (bookingTimes != null && !bookingTimes.isEmpty()) {
            log.info("Finding suitable employees for service: {}, {} booking time slots, district: {}, city: {}",
                    serviceId, bookingTimes.size(), ward, city);
        } else {
            log.info("Finding suitable employees for service: {}, booking time: {}, district: {}, city: {}",
                    serviceId, bookingTime, ward, city);
        }

        try {
            // Lấy customerId từ authentication context
            String customerId = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    String username = authentication.getName();
                    Account account = accountRepository.findByUsername(username).orElse(null);
                    if (account != null) {
                        Customer customer = customerRepository.findByAccount_AccountId(account.getAccountId()).orElse(null);
                        if (customer != null) {
                            customerId = customer.getCustomerId();
                            log.info("Found customerId: {} for username: {}", customerId, username);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Could not extract customerId from authentication: {}", e.getMessage());
            }

            SuitableEmployeeRequest request = new SuitableEmployeeRequest(serviceId, bookingTime, ward, city, customerId, bookingTimes);
            ApiResponse<List<SuitableEmployeeResponse>> response = employeeScheduleService.findSuitableEmployees(request);

            if (!response.success() || response.data() == null || response.data().isEmpty()) {
                return ResponseEntity.ok(response);
            }

            List<SuitableEmployeeResponse> rankedEmployees =
                    employeeRecommendationService.recommend(request, response.data());

            String enrichedMessage = enrichRecommendationMessage(response.message(), rankedEmployees);
            ApiResponse<List<SuitableEmployeeResponse>> enrichedResponse =
                    new ApiResponse<>(response.success(), enrichedMessage, rankedEmployees);

            return ResponseEntity.ok(enrichedResponse);
        } catch (Exception e) {
            log.error("Error in findSuitableEmployees: ", e);
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null)
            );
        }
    }

    private String enrichRecommendationMessage(String originalMessage, List<SuitableEmployeeResponse> employees) {
        if (employees == null || employees.isEmpty()) {
            return originalMessage;
        }

        // Simply return the original message without ML model info
        return (originalMessage == null || originalMessage.isBlank())
                ? "Tìm thấy nhân viên phù hợp"
                : originalMessage;
    }
}
