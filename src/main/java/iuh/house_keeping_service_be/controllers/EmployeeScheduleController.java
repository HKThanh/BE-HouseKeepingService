package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;
import iuh.house_keeping_service_be.services.EmployeeScheduleService.EmployeeScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-schedule")
@RequiredArgsConstructor
@Slf4j
public class EmployeeScheduleController {

    private final EmployeeScheduleService employeeScheduleService;

//    @GetMapping("/available")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public ResponseEntity<ApiResponse<List<EmployeeScheduleResponse>>> getAvailableEmployees(
//            @RequestParam(required = false) String district,
//            @RequestParam(required = false) String city,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
//
//        log.info("Getting available employees for district: {}, city: {}, from: {} to: {}",
//                district, city, startDate, endDate);
//
//        try {
//            EmployeeScheduleRequest request = new EmployeeScheduleRequest(null, startDate, endDate, district, city);
//            ApiResponse<List<EmployeeScheduleResponse>> response = employeeScheduleService.getAvailableEmployees(request);
//
//            if (response.success()) {
//                return ResponseEntity.ok(response);
//            } else {
//                return ResponseEntity.badRequest().body(response);
//            }
//        } catch (Exception e) {
//            log.error("Error in getAvailableEmployees: ", e);
//            ApiResponse<List<EmployeeScheduleResponse>> errorResponse =
//                new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null);
//            return ResponseEntity.internalServerError().body(errorResponse);
//        }
//    }
//
//    @GetMapping("/busy")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public ResponseEntity<ApiResponse<List<EmployeeScheduleResponse>>> getBusyEmployees(
//            @RequestParam(required = false) String district,
//            @RequestParam(required = false) String city,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
//
//        log.info("Getting busy employees for district: {}, city: {}, from: {} to: {}",
//                district, city, startDate, endDate);
//
//        try {
//            EmployeeScheduleRequest request = new EmployeeScheduleRequest(null, startDate, endDate, district, city);
//            ApiResponse<List<EmployeeScheduleResponse>> response = employeeScheduleService.getBusyEmployees(request);
//
//            if (response.success()) {
//                return ResponseEntity.ok(response);
//            } else {
//                return ResponseEntity.badRequest().body(response);
//            }
//        } catch (Exception e) {
//            log.error("Error in getBusyEmployees: ", e);
//            ApiResponse<List<EmployeeScheduleResponse>> errorResponse =
//                new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null);
//            return ResponseEntity.internalServerError().body(errorResponse);
//        }
//    }

    // Trong EmployeeScheduleController.java

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')") // Cho phép cả Customer tìm kiếm
    public ResponseEntity<ApiResponse<List<EmployeeScheduleResponse>>> getEmployeesByStatus(
            @RequestParam(defaultValue = "AVAILABLE") String status, // Mặc định là tìm nhân viên rảnh
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Getting employees with status: {} for district: {}, city: {}, from: {} to: {}",
                status, ward, city, startDate, endDate);

        try {
            EmployeeScheduleRequest request = new EmployeeScheduleRequest(null, startDate, endDate, ward, city);
            ApiResponse<List<EmployeeScheduleResponse>> response;

            if ("BUSY".equalsIgnoreCase(status)) {
                response = employeeScheduleService.getBusyEmployees(request);
            } else {
                response = employeeScheduleService.getAvailableEmployees(request);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getEmployeesByStatus: ", e);
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null)
            );
        }
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_EMPLOYEE'))")
    public ResponseEntity<ApiResponse<EmployeeScheduleResponse>> getEmployeeSchedule(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Getting schedule for employee: {} from: {} to: {}", employeeId, startDate, endDate);

        ApiResponse<EmployeeScheduleResponse> response = employeeScheduleService.getEmployeeSchedule(employeeId, startDate, endDate);

        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/unavailability")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_EMPLOYEE') and authentication.name == #request.employeeId())")
    public ResponseEntity<ApiResponse<EmployeeScheduleResponse>> createUnavailability(
            @RequestBody UnavailabilityRequest request) {

        log.info("Creating unavailability for employee: {} from: {} to: {}",
                request.employeeId(), request.startTime(), request.endTime());

        try {
            ApiResponse<EmployeeScheduleResponse> response = employeeScheduleService.createUnavailability(request);

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error in createUnavailability: ", e);
            ApiResponse<EmployeeScheduleResponse> errorResponse =
                    new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/suitable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CUSTOMER')")
    public ResponseEntity<ApiResponse<List<SuitableEmployeeResponse>>> findSuitableEmployees(
            @RequestParam Integer serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bookingTime,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String city) {

        log.info("Finding suitable employees for service: {}, booking time: {}, district: {}, city: {}",
                serviceId, bookingTime, ward, city);

        try {
            // Admin endpoint không có customerId context, nên truyền null
            SuitableEmployeeRequest request = new SuitableEmployeeRequest(serviceId, bookingTime, ward, city, null);
            ApiResponse<List<SuitableEmployeeResponse>> response = employeeScheduleService.findSuitableEmployees(request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in findSuitableEmployees: ", e);
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null)
            );
        }
    }
}