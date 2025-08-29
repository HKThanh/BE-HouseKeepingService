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
}