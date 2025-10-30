package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Booking.response.BookingResponse;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee/bookings")
@Slf4j
@RequiredArgsConstructor
public class EmployeeBookingController {
    private final BookingService bookingService;

    @GetMapping("/verified-awaiting-employee")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getVerifiedAwaitingEmployeeBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching verified bookings awaiting employee (page: {}, size: {})", page, size);
        
        try {
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;
            
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> verifiedAwaitingBookings = bookingService.getVerifiedAwaitingEmployeeBookings(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", verifiedAwaitingBookings.getContent(),
                "currentPage", verifiedAwaitingBookings.getNumber(),
                "totalItems", verifiedAwaitingBookings.getTotalElements(),
                "totalPages", verifiedAwaitingBookings.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching verified awaiting employee bookings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy danh sách booking đã xác minh đang chờ nhân viên"
            ));
        }
    }
}
