package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.config.JwtUtil;
import iuh.house_keeping_service_be.dtos.Booking.internal.BookingValidationResult;
import iuh.house_keeping_service_be.dtos.Booking.request.BookingCreateRequest;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingResponse;
import iuh.house_keeping_service_be.dtos.Booking.summary.BookingCreationSummary;
import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.models.Address;
import iuh.house_keeping_service_be.services.AddressService.AddressService;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private final AddressService addressService;
    private final PermissionService permissionService;
    private final BookingService bookingService;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingCreationSummary> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        log.info("Creating new booking with {} services", request.bookingDetails().size());
        
        BookingCreationSummary summary = bookingService.createBooking(request);

        log.info("Booking created successfully: {}", summary.getBookingId());
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        log.info("Getting booking details: {}", bookingId);
        
        BookingResponse response = bookingService.getBookingDetails(bookingId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingValidationResult> validateBooking(@Valid @RequestBody BookingCreateRequest request) {
        log.info("Validating booking request");
        
        BookingValidationResult result = bookingService.validateBooking(request);

        return ResponseEntity.ok(result);
    }
}
