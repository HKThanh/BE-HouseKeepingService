package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;
import iuh.house_keeping_service_be.services.EmployeeWorkingHoursService.EmployeeWorkingHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Controller for managing employee working hours
 * Allows employees to configure their own working schedule
 */
@RestController
@RequestMapping("/api/v1/employee-working-hours")
@RequiredArgsConstructor
@Slf4j
public class EmployeeWorkingHoursController {

    private final EmployeeWorkingHoursService workingHoursService;

    /**
     * Get all working hours for an employee
     * 
     * @param employeeId The employee ID
     * @return List of working hours for each day of the week
     */
    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<WorkingHoursResponse>>> getEmployeeWorkingHours(
            @PathVariable String employeeId) {

        log.info("Getting working hours for employee: {}", employeeId);

        try {
            ApiResponse<List<WorkingHoursResponse>> response = 
                    workingHoursService.getEmployeeWorkingHours(employeeId);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error getting employee working hours: ", e);
            ApiResponse<List<WorkingHoursResponse>> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Set or update working hours for a specific day
     * 
     * @param request The working hours request
     * @return The created/updated working hours
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<WorkingHoursResponse>> setWorkingHours(
            @RequestBody WorkingHoursRequest request) {

        log.info("Setting working hours for employee: {}, day: {}", 
                request.employeeId(), request.dayOfWeek());

        try {
            ApiResponse<WorkingHoursResponse> response = workingHoursService.setWorkingHours(request);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error setting working hours: ", e);
            ApiResponse<WorkingHoursResponse> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Set working hours for the entire week
     * 
     * @param request The batch request containing weekly schedule
     * @return List of all working hours for the week
     */
    @PostMapping("/weekly")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<WorkingHoursResponse>>> setWeeklyWorkingHours(
            @RequestBody WorkingHoursBatchRequest request) {

        log.info("Setting weekly working hours for employee: {}", request.employeeId());

        try {
            ApiResponse<List<WorkingHoursResponse>> response = 
                    workingHoursService.setWeeklyWorkingHours(request);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error setting weekly working hours: ", e);
            ApiResponse<List<WorkingHoursResponse>> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Update existing working hours
     * 
     * @param workingHoursId The ID of the working hours to update
     * @param request The updated working hours data
     * @return The updated working hours
     */
    @PutMapping("/{workingHoursId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<WorkingHoursResponse>> updateWorkingHours(
            @PathVariable String workingHoursId,
            @RequestBody WorkingHoursRequest request) {

        log.info("Updating working hours: {}", workingHoursId);

        try {
            ApiResponse<WorkingHoursResponse> response = 
                    workingHoursService.updateWorkingHours(workingHoursId, request);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error updating working hours: ", e);
            ApiResponse<WorkingHoursResponse> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Delete working hours for a specific day
     * 
     * @param workingHoursId The ID of the working hours to delete
     * @return Success status
     */
    @DeleteMapping("/{workingHoursId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> deleteWorkingHours(
            @PathVariable String workingHoursId) {

        log.info("Deleting working hours: {}", workingHoursId);

        try {
            ApiResponse<Void> response = workingHoursService.deleteWorkingHours(workingHoursId);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error deleting working hours: ", e);
            ApiResponse<Void> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Initialize default working hours for an employee
     * Creates working hours for all days with default times (Mon-Sat 8:00-18:00, Sun off)
     * 
     * @param employeeId The employee ID
     * @return List of created working hours
     */
    @PostMapping("/{employeeId}/initialize")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<WorkingHoursResponse>>> initializeDefaultWorkingHours(
            @PathVariable String employeeId) {

        log.info("Initializing default working hours for employee: {}", employeeId);

        try {
            ApiResponse<List<WorkingHoursResponse>> response = 
                    workingHoursService.initializeDefaultWorkingHours(employeeId);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error initializing default working hours: ", e);
            ApiResponse<List<WorkingHoursResponse>> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Copy working hours from one day to another
     * 
     * @param employeeId The employee ID
     * @param sourceDay The day to copy from
     * @param targetDay The day to copy to
     * @return The copied working hours
     */
    @PostMapping("/{employeeId}/copy")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<WorkingHoursResponse>> copyWorkingHours(
            @PathVariable String employeeId,
            @RequestParam DayOfWeek sourceDay,
            @RequestParam DayOfWeek targetDay) {

        log.info("Copying working hours for employee: {} from {} to {}", 
                employeeId, sourceDay, targetDay);

        try {
            ApiResponse<WorkingHoursResponse> response = 
                    workingHoursService.copyWorkingHours(employeeId, sourceDay, targetDay);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error copying working hours: ", e);
            ApiResponse<WorkingHoursResponse> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
