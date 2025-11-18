package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingCancelRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.request.RecurringBookingCreateRequest;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingCreationSummary;
import iuh.house_keeping_service_be.dtos.RecurringBooking.response.RecurringBookingResponse;
import iuh.house_keeping_service_be.services.RecurringBookingService.RecurringBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing recurring bookings
 */
@RestController
@RequestMapping("/api/v1/customer/recurring-bookings")
@Slf4j
@RequiredArgsConstructor
public class RecurringBookingController {

    private final RecurringBookingService recurringBookingService;

    /**
     * Create a new recurring booking
     * POST /api/v1/customer/recurring-bookings/{customerId}
     */
    @PostMapping("/{customerId}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> createRecurringBooking(
            @PathVariable String customerId,
            @Valid @RequestBody RecurringBookingCreateRequest request
    ) {
        try {
            log.info("Creating recurring booking for customer: {}", customerId);

            RecurringBookingCreationSummary summary = recurringBookingService
                    .createRecurringBooking(request, customerId);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "success", true,
                            "message", summary.getMessage(),
                            "data", summary
                    )
            );

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating recurring booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            log.error("Error creating recurring booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Lỗi tạo lịch định kỳ: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Cancel a recurring booking and delete all future bookings
     * PUT /api/v1/customer/recurring-bookings/{customerId}/{recurringBookingId}/cancel
     */
    @PutMapping("/{customerId}/{recurringBookingId}/cancel")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> cancelRecurringBooking(
            @PathVariable String customerId,
            @PathVariable String recurringBookingId,
            @Valid @RequestBody RecurringBookingCancelRequest request
    ) {
        try {
            log.info("Cancelling recurring booking: {} for customer: {}", recurringBookingId, customerId);

            RecurringBookingResponse response = recurringBookingService
                    .cancelRecurringBooking(recurringBookingId, customerId, request);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Đã hủy lịch định kỳ thành công",
                            "data", response
                    )
            );

        } catch (IllegalArgumentException e) {
            log.error("Validation error cancelling recurring booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            log.error("Error cancelling recurring booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Lỗi hủy lịch định kỳ: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Get all recurring bookings for the current customer
     * GET /api/v1/customer/recurring-bookings/{customerId}
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> getMyRecurringBookings(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("Getting recurring bookings for customer: {}", customerId);

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<RecurringBookingResponse> recurringBookings = recurringBookingService
                    .getRecurringBookingsByCustomer(customerId, pageable);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", recurringBookings.getContent(),
                            "currentPage", recurringBookings.getNumber(),
                            "totalItems", recurringBookings.getTotalElements(),
                            "totalPages", recurringBookings.getTotalPages()
                    )
            );

        } catch (Exception e) {
            log.error("Error getting recurring bookings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Lỗi lấy danh sách lịch định kỳ: " + e.getMessage()
                    )
            );
        }
    }

    /**
     * Get details of a specific recurring booking
     * GET /api/v1/customer/recurring-bookings/{customerId}/{recurringBookingId}
     */
    @GetMapping("/{customerId}/{recurringBookingId}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> getRecurringBookingDetails(
            @PathVariable String customerId,
            @PathVariable String recurringBookingId
    ) {
        try {
            log.info("Getting recurring booking details: {} for customer: {}", recurringBookingId, customerId);

            RecurringBookingResponse response = recurringBookingService
                    .getRecurringBookingDetails(recurringBookingId, customerId);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", response
                    )
            );

        } catch (IllegalArgumentException e) {
            log.error("Validation error getting recurring booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            log.error("Error getting recurring booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "success", false,
                            "message", "Lỗi lấy thông tin lịch định kỳ: " + e.getMessage()
                    )
            );
        }
    }
}
