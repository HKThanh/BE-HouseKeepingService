package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.models.Address;
import iuh.house_keeping_service_be.services.AddressService.AddressService;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private final AddressService addressService;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;

    @GetMapping("/{customerId}/default-address")
    public ResponseEntity<?> getDefaultAddress(@PathVariable String customerId
                                                , @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new ServiceDetailResponse(false, "Token không hợp lệ", null)
                );
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // Kiểm tra quyền xem chi tiết dịch vụ
            if (!permissionService.hasPermission(username, "booking.create")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ServiceDetailResponse(false, "Không có quyền đặt dịch vụ", null)
                );
            }

            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(401).body(
                        Map.of(
                                "success", false,
                                "message", "Token không hợp lệ"
                        )
                );
            }

            Address address = addressService.findByCustomerId(customerId);
            if (address != null) {
                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "data", address
                        )
                );
            }
            return ResponseEntity.status(404).body(
                    Map.of(
                            "success", false,
                            "message", "Khách hàng chưa có địa chỉ mặc định: " + customerId
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    Map.of(
                            "success", false,
                            "message", "Token không hợp lệ"
                    )
            );
        }
    }
}
