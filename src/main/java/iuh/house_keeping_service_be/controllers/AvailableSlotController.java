package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;
import iuh.house_keeping_service_be.services.AvailableSlotService.AvailableSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for managing available booking slots
 * Provides endpoints to query available time slots based on date, location, and service
 */
@RestController
@RequestMapping("/api/v1/available-slots")
@RequiredArgsConstructor
@Slf4j
public class AvailableSlotController {

    private final AvailableSlotService availableSlotService;

    /**
     * Get available slots for a specific date
     * 
     * @param date The date to check for available slots
     * @param ward The ward/phường (optional)
     * @param city The city (optional)
     * @param serviceId The service ID to get duration (optional if durationMinutes is provided)
     * @param durationMinutes The duration in minutes (optional if serviceId is provided)
     * @param slotIntervalMinutes The interval between slots in minutes (default: 30)
     * @return List of available time slots with available employees
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<DailyAvailableSlotsResponse>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer serviceId,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(defaultValue = "30") Integer slotIntervalMinutes) {

        log.info("Getting available slots for date: {}, ward: {}, city: {}, serviceId: {}, duration: {}min, interval: {}min",
                date, ward, city, serviceId, durationMinutes, slotIntervalMinutes);

        try {
            AvailableSlotsRequest request = new AvailableSlotsRequest(
                    date, ward, city, serviceId, durationMinutes, slotIntervalMinutes);
            
            ApiResponse<DailyAvailableSlotsResponse> response = availableSlotService.getAvailableSlots(request);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error in getAvailableSlots: ", e);
            ApiResponse<DailyAvailableSlotsResponse> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get available slots for a date range
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range (max 30 days from start)
     * @param ward The ward/phường (optional)
     * @param city The city (optional)
     * @param serviceId The service ID to get duration (optional)
     * @param durationMinutes The duration in minutes (optional)
     * @param slotIntervalMinutes The interval between slots in minutes (default: 30)
     * @return List of daily available slots
     */
    @GetMapping("/range")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<DailyAvailableSlotsResponse>>> getAvailableSlotsForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer serviceId,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(defaultValue = "30") Integer slotIntervalMinutes) {

        log.info("Getting available slots from {} to {}, ward: {}, city: {}",
                startDate, endDate, ward, city);

        try {
            ApiResponse<List<DailyAvailableSlotsResponse>> response = 
                    availableSlotService.getAvailableSlotsForRange(
                            startDate, endDate, ward, city, serviceId, durationMinutes, slotIntervalMinutes);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error in getAvailableSlotsForRange: ", e);
            ApiResponse<List<DailyAvailableSlotsResponse>> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Check if a specific time slot is available
     * 
     * @param startTime Start time of the slot
     * @param endTime End time of the slot
     * @param ward The ward/phường (optional)
     * @param city The city (optional)
     * @param minEmployees Minimum number of employees required (default: 1)
     * @return Whether the slot is available
     */
    @GetMapping("/check")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<Boolean>> checkSlotAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") Integer minEmployees) {

        log.info("Checking slot availability from {} to {}, ward: {}, city: {}, minEmployees: {}",
                startTime, endTime, ward, city, minEmployees);

        try {
            ApiResponse<Boolean> response = availableSlotService.isSlotAvailable(
                    startTime, endTime, ward, city, minEmployees);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in checkSlotAvailability: ", e);
            ApiResponse<Boolean> errorResponse =
                    new ApiResponse<>(false, "Lỗi server: " + e.getMessage(), false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
