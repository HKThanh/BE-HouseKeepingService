package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCancelRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.services.AssignmentService.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeAssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/{employeeId}/assignments")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN')")
    public ResponseEntity<?> getEmployeeAssignments(
            @PathVariable String employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<AssignmentDetailResponse> assignments = assignmentService
                    .getEmployeeAssignments(employeeId, status, page, size);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy danh sách công việc thành công",
                    "data", assignments,
                    "totalItems", assignments.size()
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
}