package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Booking.request.BookingVerificationRequest;
import iuh.house_keeping_service_be.dtos.Booking.request.UpdateBookingStatusRequest;
import iuh.house_keeping_service_be.dtos.Booking.response.BookingResponse;
import iuh.house_keeping_service_be.dtos.Statistics.RevenueStatisticsResponse;
import iuh.house_keeping_service_be.dtos.Statistics.ServiceBookingStatisticsResponse;
import iuh.house_keeping_service_be.services.AdminService.AdminService;
import iuh.house_keeping_service_be.services.AuthorizationService.AuthorizationService;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/{adminId}")
    public ResponseEntity<?> getAdminById(@PathVariable String adminId,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            // Check if user can access this admin resource
            if (!authorizationService.canAccessResource(authHeader, adminId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Access denied. You can only access your own data."
                ));
            }

            var admin = adminService.findById(adminId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", admin
            ));
        } catch (IllegalArgumentException e) {
            log.error("Admin not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching admin profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy thông tin quản trị viên"
            ));
        }
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin fetching all bookings sorted by booking time (page: {}, size: {})", page, size);
        
        try {
            // Validate pagination parameters
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;
            
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> allBookings = bookingService.getAllBookingsSortedByBookingTime(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", allBookings.getContent(),
                "currentPage", allBookings.getNumber(),
                "totalItems", allBookings.getTotalElements(),
                "totalPages", allBookings.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching all bookings: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy danh sách booking"
            ));
        }
    }

    @GetMapping("/bookings/unverified")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUnverifiedBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin fetching unverified bookings (page: {}, size: {})", page, size);
        
        try {
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;
            
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> unverifiedBookings = bookingService.getUnverifiedBookings(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", unverifiedBookings.getContent(),
                "currentPage", unverifiedBookings.getNumber(),
                "totalItems", unverifiedBookings.getTotalElements(),
                "totalPages", unverifiedBookings.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching unverified bookings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy danh sách booking chưa xác minh"
            ));
        }
    }

    @PutMapping("/bookings/{bookingId}/verify")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> verifyBooking(
            @PathVariable String bookingId,
            @Valid @RequestBody BookingVerificationRequest request) {
        
        log.info("Admin verifying booking {}: approve={}", bookingId, request.approve());
        
        try {
            BookingResponse response = bookingService.verifyBooking(bookingId, request);
            
            String message = request.approve() 
                ? "Chấp nhận bài post thành công" 
                : "Từ chối bài post thành công";
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "data", response
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error verifying booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error verifying booking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi xác minh booking"
            ));
        }
    }

    @PutMapping("/bookings/{bookingId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable String bookingId,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        
        log.info("Admin updating booking {} status to {}", bookingId, request.getStatus());
        
        try {
            BookingResponse response = bookingService.updateBookingStatus(bookingId, request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật trạng thái booking thành công",
                "data", response
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating booking status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error updating booking status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi cập nhật trạng thái booking"
            ));
        }
    }

    @GetMapping("/statistics/service-bookings")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getServiceBookingStatistics(
            @RequestParam(required = false, defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Admin getting service booking statistics - period: {}, startDate: {}, endDate: {}", 
                 period, startDate, endDate);
        
        try {
            // Validate period
            if (period != null && startDate == null && endDate == null) {
                String upperPeriod = period.toUpperCase();
                if (!upperPeriod.equals("DAY") && !upperPeriod.equals("WEEK") && 
                    !upperPeriod.equals("MONTH") && !upperPeriod.equals("QUARTER") && 
                    !upperPeriod.equals("YEAR")) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Period phải là DAY, WEEK, MONTH, QUARTER, hoặc YEAR"
                    ));
                }
            }

            // Validate custom date range
            if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Phải cung cấp cả startDate và endDate cho khoảng thời gian tùy chỉnh"
                ));
            }

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "startDate không thể sau endDate"
                ));
            }

            ServiceBookingStatisticsResponse statistics = adminService.getServiceBookingStatistics(
                    period, startDate, endDate);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", statistics
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error getting service booking statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy thống kê dịch vụ"
            ));
        }
    }

    @GetMapping("/statistics/revenue")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getRevenueStatistics(
            @RequestParam(required = false, defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Admin getting revenue statistics - period: {}, startDate: {}, endDate: {}", 
                 period, startDate, endDate);
        
        try {
            // Validate period
            if (period != null && startDate == null && endDate == null) {
                String upperPeriod = period.toUpperCase();
                if (!upperPeriod.equals("DAY") && !upperPeriod.equals("WEEK") && 
                    !upperPeriod.equals("MONTH") && !upperPeriod.equals("QUARTER") && 
                    !upperPeriod.equals("YEAR")) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Period phải là DAY, WEEK, MONTH, QUARTER, hoặc YEAR"
                    ));
                }
            }

            // Validate custom date range
            if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Phải cung cấp cả startDate và endDate cho khoảng thời gian tùy chỉnh"
                ));
            }

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "startDate không thể sau endDate"
                ));
            }

            RevenueStatisticsResponse statistics = adminService.getRevenueStatistics(
                    period, startDate, endDate);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", statistics
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for revenue statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error getting revenue statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy thống kê doanh thu"
            ));
        }
    }

    @GetMapping("/bookings/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        log.info("Getting booking details: {}", bookingId);
        
        BookingResponse response = bookingService.getBookingDetails(bookingId);

        return ResponseEntity.ok(response);
    }
}