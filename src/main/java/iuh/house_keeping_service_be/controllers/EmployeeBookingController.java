package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Booking.response.BookingResponse;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.services.BookingService.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee/bookings")
@Slf4j
@RequiredArgsConstructor
public class EmployeeBookingController {
    private final BookingService bookingService;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/details/{bookingId}")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable String bookingId) {
        log.info("Employee getting booking details: {}", bookingId);
        
        BookingResponse response = bookingService.getBookingDetails(bookingId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getEmployeeAssignedBookings(
            @PathVariable String employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching assigned bookings for employee: {} (page: {}, size: {})", employeeId, page, size);
        
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Find account by username
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

            // Check if user is admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            // If not admin, check if the employeeId matches the authenticated employee
            if (!isAdmin) {
                Employee currentEmployee = employeeRepository.findByAccount_AccountId(account.getAccountId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));
                
                if (!currentEmployee.getEmployeeId().equals(employeeId)) {
                    log.warn("Employee {} attempted to access bookings of employee {}", 
                            currentEmployee.getEmployeeId(), employeeId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền truy cập thông tin của nhân viên khác"
                    ));
                }
            }

            // Verify that the requested employeeId exists
            employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));

            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;
            
            Pageable pageable = PageRequest.of(page, size);
            Page<BookingResponse> employeeBookings = bookingService.getBookingsByEmployeeId(employeeId, pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", employeeBookings.getContent(),
                "currentPage", employeeBookings.getNumber(),
                "totalItems", employeeBookings.getTotalElements(),
                "totalPages", employeeBookings.getTotalPages()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching employee assigned bookings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi lấy danh sách booking được phân công"
            ));
        }
    }

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
