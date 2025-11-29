package iuh.house_keeping_service_be.services.EmployeeScheduleService;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;
import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeScheduleService {
    ApiResponse<List<EmployeeScheduleResponse>> getAvailableEmployees(EmployeeScheduleRequest request);
    ApiResponse<List<EmployeeScheduleResponse>> getBusyEmployees(EmployeeScheduleRequest request);
    ApiResponse<EmployeeScheduleResponse> getEmployeeSchedule(String employeeId, LocalDateTime startDate, LocalDateTime endDate);
    ApiResponse<EmployeeScheduleResponse> createUnavailability(UnavailabilityRequest request);
    ApiResponse<List<SuitableEmployeeResponse>> findSuitableEmployees(SuitableEmployeeRequest request);
    
    /**
     * Check if employee is available within their working hours and has no conflicts
     * @param employeeId The employee ID
     * @param startTime The start time of the booking
     * @param endTime The end time of the booking
     * @param checkWorkingHours Whether to also check against employee's configured working hours
     * @return true if employee is available
     */
    boolean isEmployeeAvailableWithWorkingHours(String employeeId, LocalDateTime startTime, LocalDateTime endTime, boolean checkWorkingHours);
}