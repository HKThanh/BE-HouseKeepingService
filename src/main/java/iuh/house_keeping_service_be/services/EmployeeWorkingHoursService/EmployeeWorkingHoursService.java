package iuh.house_keeping_service_be.services.EmployeeWorkingHoursService;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;

import java.util.List;

/**
 * Service interface for managing employee working hours
 */
public interface EmployeeWorkingHoursService {

    /**
     * Get all working hours for an employee
     */
    ApiResponse<List<WorkingHoursResponse>> getEmployeeWorkingHours(String employeeId);

    /**
     * Set working hours for a specific day
     */
    ApiResponse<WorkingHoursResponse> setWorkingHours(WorkingHoursRequest request);

    /**
     * Set working hours for entire week (batch operation)
     */
    ApiResponse<List<WorkingHoursResponse>> setWeeklyWorkingHours(WorkingHoursBatchRequest request);

    /**
     * Update existing working hours
     */
    ApiResponse<WorkingHoursResponse> updateWorkingHours(String workingHoursId, WorkingHoursRequest request);

    /**
     * Delete working hours for a specific day
     */
    ApiResponse<Void> deleteWorkingHours(String workingHoursId);

    /**
     * Initialize default working hours for a new employee
     */
    ApiResponse<List<WorkingHoursResponse>> initializeDefaultWorkingHours(String employeeId);

    /**
     * Copy working hours from one day to another
     */
    ApiResponse<WorkingHoursResponse> copyWorkingHours(String employeeId, 
                                                        java.time.DayOfWeek sourceDay, 
                                                        java.time.DayOfWeek targetDay);
}
