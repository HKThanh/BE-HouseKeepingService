package iuh.house_keeping_service_be.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCancelRequest;
import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCheckInRequest;
import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCheckOutRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentActionResponse;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.dtos.Assignment.response.BookingSummary;
import iuh.house_keeping_service_be.services.AssignmentService.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeAssignmentController {

    private final AssignmentService assignmentService;
    private final ObjectMapper objectMapper;

    @GetMapping("/{employeeId}/assignments")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getEmployeeAssignments(
            @PathVariable String employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            var pageResponse = assignmentService
                    .getEmployeeAssignments(employeeId, status, page, size);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy danh sách công việc thành công",
                    "data", pageResponse.getContent(),
                    "pagination", Map.of(
                            "currentPage", pageResponse.getCurrentPage(),
                            "pageSize", pageResponse.getPageSize(),
                            "totalItems", pageResponse.getTotalItems(),
                            "totalPages", pageResponse.getTotalPages(),
                            "hasNext", pageResponse.isHasNext(),
                            "hasPrevious", pageResponse.isHasPrevious()
                    )
            ));

        } catch (Exception e) {
            log.error("Error fetching employee assignments: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách công việc: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/assignments/{assignmentId}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> cancelAssignment(
            @PathVariable String assignmentId,
            @Valid @RequestBody AssignmentCancelRequest request) {

        try {
            boolean cancelled = assignmentService.cancelAssignment(assignmentId, request);

            if (cancelled) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Hủy công việc thành công. Hệ thống sẽ thông báo cho khách hàng."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không thể hủy công việc này"
                ));
            }

        } catch (IllegalStateException e) {
            log.warn("Assignment cancellation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("Error cancelling assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi hủy công việc: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/assignments/{assignmentId}/accept")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public ResponseEntity<?> acceptAssignment(
            @PathVariable String assignmentId,
            @RequestParam String employeeId) {

        try {
            AssignmentDetailResponse response = assignmentService.acceptAssignment(assignmentId, employeeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Nhận công việc thành công",
                    "data", response
            ));
        } catch (IllegalStateException e) {
            log.warn("Accept assignment failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request when accepting assignment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error accepting assignment: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi nhận công việc: " + e.getMessage()
            ));
        }
    }


    @GetMapping("/available-bookings")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getAvailableBookings(
            @RequestParam String employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<BookingSummary> bookings = assignmentService.getAvailableBookings(employeeId, page, size);

            String message = bookings.isEmpty() ? "Không có booking chờ" : "Lấy danh sách booking chờ thành công";
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", message,
                    "data", bookings,
                    "totalItems", bookings.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching available bookings: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi lấy danh sách booking chờ: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/booking-details/{detailId}/accept")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public ResponseEntity<?> acceptBookingDetail(
            @PathVariable String detailId,
            @RequestParam String employeeId) {

        try {
            AssignmentDetailResponse response = assignmentService.acceptBookingDetail(detailId, employeeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Nhận công việc thành công",
                    "data", response
            ));
        } catch (IllegalStateException e) {
            log.warn("Accept booking detail failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request when accepting booking detail: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error accepting booking detail: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi nhận booking: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/assignments/{assignmentId}/check-in")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public ResponseEntity<?> checkInAssignment(
            @PathVariable String assignmentId,
            @RequestPart(value = "request", required = true) String requestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            // Parse JSON string to AssignmentCheckInRequest object
            AssignmentCheckInRequest request;
            try {
                request = objectMapper.readValue(requestJson, AssignmentCheckInRequest.class);
            } catch (Exception e) {
                log.error("Failed to parse check-in request JSON: {}", e.getMessage());
                return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                        false,
                        "Dữ liệu yêu cầu không hợp lệ: " + e.getMessage(),
                        null
                ));
            }

            // Validate images if provided
            if (images != null && !images.isEmpty()) {
                if (images.size() > 10) {
                    return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                            false,
                            "Số lượng ảnh không được vượt quá 10",
                            null
                    ));
                }

                for (MultipartFile image : images) {
                    String contentType = image.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                                false,
                                "Tất cả file phải là định dạng ảnh",
                                null
                        ));
                    }

                    if (image.getSize() > 10 * 1024 * 1024) {
                        return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                                false,
                                "Kích thước mỗi file không được vượt quá 10MB",
                                null
                        ));
                    }
                }
            }

            AssignmentDetailResponse response = assignmentService.checkIn(
                    assignmentId, 
                    request.employeeId(), 
                    images, 
                    request.imageDescription(),
                    request.latitude(),
                    request.longitude()
            );

            return ResponseEntity.ok(new AssignmentActionResponse(
                    true,
                    "Điểm danh bắt đầu công việc thành công",
                    response
            ));
        } catch (IllegalStateException e) {
            log.warn("Check-in assignment failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Check-in assignment invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            log.error("Error when checking in assignment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new AssignmentActionResponse(
                    false,
                    "Lỗi khi điểm danh công việc: " + e.getMessage(),
                    null
            ));
        }
    }

    @PostMapping("/assignments/{assignmentId}/check-out")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public ResponseEntity<?> checkOutAssignment(
            @PathVariable String assignmentId,
            @RequestPart(value = "request", required = true) String requestJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            // Parse JSON string to AssignmentCheckOutRequest object
            AssignmentCheckOutRequest request;
            try {
                request = objectMapper.readValue(requestJson, AssignmentCheckOutRequest.class);
            } catch (Exception e) {
                log.error("Failed to parse check-out request JSON: {}", e.getMessage());
                return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                        false,
                        "Dữ liệu yêu cầu không hợp lệ: " + e.getMessage(),
                        null
                ));
            }

            // Validate images if provided
            if (images != null && !images.isEmpty()) {
                if (images.size() > 10) {
                    return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                            false,
                            "Số lượng ảnh không được vượt quá 10",
                            null
                    ));
                }

                for (MultipartFile image : images) {
                    String contentType = image.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                                false,
                                "Tất cả file phải là định dạng ảnh",
                                null
                        ));
                    }

                    if (image.getSize() > 10 * 1024 * 1024) {
                        return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                                false,
                                "Kích thước mỗi file không được vượt quá 10MB",
                                null
                        ));
                    }
                }
            }

            AssignmentDetailResponse response = assignmentService.checkOut(
                    assignmentId, 
                    request.employeeId(), 
                    images, 
                    request.imageDescription(),
                    request.latitude(),
                    request.longitude()
            );

            return ResponseEntity.ok(new AssignmentActionResponse(
                    true,
                    "Chấm công kết thúc công việc thành công",
                    response
            ));
        } catch (IllegalStateException e) {
            log.warn("Check-out assignment failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Check-out assignment invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AssignmentActionResponse(
                    false,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            log.error("Error when checking out assignment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new AssignmentActionResponse(
                    false,
                    "Lỗi khi chấm công công việc: " + e.getMessage(),
                    null
            ));
        }
    }
}