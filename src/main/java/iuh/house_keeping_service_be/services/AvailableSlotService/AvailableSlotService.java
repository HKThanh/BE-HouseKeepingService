package iuh.house_keeping_service_be.services.AvailableSlotService;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for calculating available time slots
 */
public interface AvailableSlotService {

    /**
     * Get available slots for a specific date, location, and service
     */
    ApiResponse<DailyAvailableSlotsResponse> getAvailableSlots(AvailableSlotsRequest request);

    /**
     * Get available slots for a date range
     */
    ApiResponse<List<DailyAvailableSlotsResponse>> getAvailableSlotsForRange(
            LocalDate startDate,
            LocalDate endDate,
            String ward,
            String city,
            Integer serviceId,
            Integer durationMinutes,
            Integer slotIntervalMinutes
    );

    /**
     * Check if a specific time slot is available for booking
     */
    ApiResponse<Boolean> isSlotAvailable(
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime,
            String ward,
            String city,
            Integer minEmployeesRequired
    );
}
