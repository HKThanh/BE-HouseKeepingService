package iuh.house_keeping_service_be.services.AvailableSlotService.impl;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.AvailableSlotService.AvailableSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AvailableSlotServiceImpl implements AvailableSlotService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkingHoursRepository workingHoursRepository;
    private final EmployeeWorkingZoneRepository workingZoneRepository;
    private final AssignmentRepository assignmentRepository;
    private final EmployeeUnavailabilityRepository unavailabilityRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewDetailRepository reviewDetailRepository;

    // Default travel time buffer in minutes between assignments
    private static final int DEFAULT_TRAVEL_TIME_MINUTES = 30;
    
    // Minimum slot duration in minutes
    private static final int MIN_SLOT_DURATION_MINUTES = 30;
    
    // Default service duration in minutes when not specified
    private static final int DEFAULT_SERVICE_DURATION_MINUTES = 60;

    @Override
    public ApiResponse<DailyAvailableSlotsResponse> getAvailableSlots(AvailableSlotsRequest request) {
        try {
            log.info("Getting available slots for date: {}, ward: {}, city: {}, serviceId: {}",
                    request.date(), request.ward(), request.city(), request.serviceId());

            // Validate date is not in the past
            if (request.date().isBefore(LocalDate.now())) {
                return new ApiResponse<>(false, "Ngày tìm kiếm không thể ở quá khứ", null);
            }

            // Get duration from service or request
            int durationMinutes = getDurationMinutes(request);
            if (durationMinutes <= 0) {
                return new ApiResponse<>(false, "Thời lượng dịch vụ không hợp lệ", null);
            }

            int slotInterval = request.slotIntervalMinutes() != null ? request.slotIntervalMinutes() : 30;

            // Get day of week for the date
            DayOfWeek dayOfWeek = request.date().getDayOfWeek();

            // Get employees in working zone
            List<Employee> employeesInZone = getEmployeesInWorkingZone(request.ward(), request.city());
            if (employeesInZone.isEmpty()) {
                return new ApiResponse<>(true, "Không có nhân viên nào trong khu vực này", 
                        new DailyAvailableSlotsResponse(
                                request.date(),
                                getDayOfWeekDisplay(dayOfWeek),
                                0, 0, Collections.emptyList()));
            }

            log.info("Found {} employees in working zone", employeesInZone.size());

            // Calculate available slots
            List<AvailableSlotResponse> slots = calculateAvailableSlots(
                    request.date(),
                    dayOfWeek,
                    employeesInZone,
                    durationMinutes,
                    slotInterval
            );

            // Count total unique employees across all slots
            Set<String> uniqueEmployees = slots.stream()
                    .flatMap(slot -> slot.availableEmployees().stream())
                    .map(AvailableSlotResponse.AvailableEmployeeInfo::employeeId)
                    .collect(Collectors.toSet());

            DailyAvailableSlotsResponse response = new DailyAvailableSlotsResponse(
                    request.date(),
                    getDayOfWeekDisplay(dayOfWeek),
                    slots.size(),
                    uniqueEmployees.size(),
                    slots
            );

            String message = slots.isEmpty() 
                    ? "Không có slot nào khả dụng trong ngày này"
                    : String.format("Tìm thấy %d slot khả dụng với %d nhân viên", slots.size(), uniqueEmployees.size());

            return new ApiResponse<>(true, message, response);

        } catch (Exception e) {
            log.error("Error getting available slots: ", e);
            return new ApiResponse<>(false, "Lỗi khi tìm slot khả dụng: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<DailyAvailableSlotsResponse>> getAvailableSlotsForRange(
            LocalDate startDate,
            LocalDate endDate,
            String ward,
            String city,
            Integer serviceId,
            Integer durationMinutes,
            Integer slotIntervalMinutes) {
        try {
            log.info("Getting available slots from {} to {}", startDate, endDate);

            if (startDate.isAfter(endDate)) {
                return new ApiResponse<>(false, "Ngày bắt đầu phải trước ngày kết thúc", null);
            }

            // Limit range to prevent performance issues
            if (startDate.plusDays(30).isBefore(endDate)) {
                return new ApiResponse<>(false, "Khoảng thời gian tìm kiếm không được vượt quá 30 ngày", null);
            }

            List<DailyAvailableSlotsResponse> results = new ArrayList<>();
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate)) {
                AvailableSlotsRequest request = new AvailableSlotsRequest(
                        currentDate, ward, city, serviceId, durationMinutes, slotIntervalMinutes);
                
                ApiResponse<DailyAvailableSlotsResponse> dayResponse = getAvailableSlots(request);
                if (dayResponse.success() && dayResponse.data() != null) {
                    results.add(dayResponse.data());
                }
                
                currentDate = currentDate.plusDays(1);
            }

            return new ApiResponse<>(true, 
                    String.format("Tìm thấy slot khả dụng cho %d ngày", results.size()), 
                    results);

        } catch (Exception e) {
            log.error("Error getting available slots for range: ", e);
            return new ApiResponse<>(false, "Lỗi khi tìm slot khả dụng: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Boolean> isSlotAvailable(
            LocalDateTime startTime,
            LocalDateTime endTime,
            String ward,
            String city,
            Integer minEmployeesRequired) {
        try {
            // Get employees in zone
            List<Employee> employeesInZone = getEmployeesInWorkingZone(ward, city);
            
            int availableCount = 0;
            for (Employee employee : employeesInZone) {
                if (isEmployeeAvailableForSlot(employee.getEmployeeId(), startTime, endTime)) {
                    availableCount++;
                    if (availableCount >= minEmployeesRequired) {
                        return new ApiResponse<>(true, "Slot khả dụng", true);
                    }
                }
            }

            return new ApiResponse<>(true, 
                    String.format("Chỉ có %d nhân viên khả dụng (yêu cầu %d)", availableCount, minEmployeesRequired), 
                    false);

        } catch (Exception e) {
            log.error("Error checking slot availability: ", e);
            return new ApiResponse<>(false, "Lỗi khi kiểm tra slot: " + e.getMessage(), false);
        }
    }

    /**
     * Calculate available time slots for a given date
     */
    private List<AvailableSlotResponse> calculateAvailableSlots(
            LocalDate date,
            DayOfWeek dayOfWeek,
            List<Employee> employees,
            int durationMinutes,
            int slotInterval) {

        List<AvailableSlotResponse> slots = new ArrayList<>();
        
        // Find earliest start and latest end from all employees' working hours
        LocalTime earliestStart = LocalTime.of(23, 59);
        LocalTime latestEnd = LocalTime.of(0, 0);
        
        Map<String, EmployeeWorkingHours> employeeWorkingHoursMap = new HashMap<>();
        
        for (Employee employee : employees) {
            Optional<EmployeeWorkingHours> workingHoursOpt = 
                    workingHoursRepository.findByEmployee_EmployeeIdAndDayOfWeek(
                            employee.getEmployeeId(), dayOfWeek);
            
            if (workingHoursOpt.isPresent() && workingHoursOpt.get().getIsWorkingDay()) {
                EmployeeWorkingHours wh = workingHoursOpt.get();
                employeeWorkingHoursMap.put(employee.getEmployeeId(), wh);
                
                if (wh.getStartTime().isBefore(earliestStart)) {
                    earliestStart = wh.getStartTime();
                }
                if (wh.getEndTime().isAfter(latestEnd)) {
                    latestEnd = wh.getEndTime();
                }
            }
        }

        // If no employees are working this day, return empty list
        if (employeeWorkingHoursMap.isEmpty()) {
            log.info("No employees have working hours on {}", dayOfWeek);
            return slots;
        }

        // For today, start from current time (rounded up to next interval)
        LocalTime slotStart = earliestStart;
        if (date.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            if (now.isAfter(earliestStart)) {
                // Round up to next slot interval
                int minutes = now.getMinute();
                int roundedMinutes = ((minutes / slotInterval) + 1) * slotInterval;
                if (roundedMinutes >= 60) {
                    slotStart = LocalTime.of(now.getHour() + 1, roundedMinutes - 60);
                } else {
                    slotStart = LocalTime.of(now.getHour(), roundedMinutes);
                }
            }
        }

        // Generate slots
        while (slotStart.plusMinutes(durationMinutes).isBefore(latestEnd) || 
               slotStart.plusMinutes(durationMinutes).equals(latestEnd)) {
            
            LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);
            LocalDateTime slotStartDateTime = LocalDateTime.of(date, slotStart);
            LocalDateTime slotEndDateTime = LocalDateTime.of(date, slotEnd);

            // Find available employees for this slot
            List<AvailableSlotResponse.AvailableEmployeeInfo> availableEmployees = new ArrayList<>();
            
            for (Employee employee : employees) {
                EmployeeWorkingHours wh = employeeWorkingHoursMap.get(employee.getEmployeeId());
                if (wh == null) continue;

                // Check if slot is within employee's working hours
                if (!isWithinWorkingHours(wh, slotStart, slotEnd)) {
                    continue;
                }

                // Check for conflicts (assignments, unavailability) with travel time buffer
                if (isEmployeeAvailableForSlot(employee.getEmployeeId(), slotStartDateTime, slotEndDateTime)) {
                    Double avgRating = fetchAverageEmployeeRating(employee.getEmployeeId());
                    String ratingStr = avgRating != null ? String.format(Locale.US, "%.2f", avgRating) : "0.00";
                    
                    availableEmployees.add(new AvailableSlotResponse.AvailableEmployeeInfo(
                            employee.getEmployeeId(),
                            employee.getFullName(),
                            employee.getAvatar(),
                            ratingStr,
                            employee.getSkills() != null ? employee.getSkills() : Collections.emptyList()
                    ));
                }
            }

            // Only add slot if there are available employees
            if (!availableEmployees.isEmpty()) {
                slots.add(new AvailableSlotResponse(
                        slotStartDateTime,
                        slotEndDateTime,
                        durationMinutes,
                        availableEmployees.size(),
                        availableEmployees
                ));
            }

            // Move to next slot
            slotStart = slotStart.plusMinutes(slotInterval);
        }

        return slots;
    }

    /**
     * Check if a time range is within employee's working hours (excluding break time)
     */
    private boolean isWithinWorkingHours(EmployeeWorkingHours wh, LocalTime start, LocalTime end) {
        if (!wh.getIsWorkingDay()) {
            return false;
        }

        // Check main working hours
        if (start.isBefore(wh.getStartTime()) || end.isAfter(wh.getEndTime())) {
            return false;
        }

        // Check break time overlap
        if (wh.getBreakStartTime() != null && wh.getBreakEndTime() != null) {
            boolean overlapsBreak = start.isBefore(wh.getBreakEndTime()) && end.isAfter(wh.getBreakStartTime());
            return !overlapsBreak;
        }

        return true;
    }

    /**
     * Check if employee is available for a specific time slot
     * Includes travel time buffer check
     */
    private boolean isEmployeeAvailableForSlot(String employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        // Check unavailability
        boolean hasUnavailabilityConflict = unavailabilityRepository.hasConflict(employeeId, startTime, endTime);
        if (hasUnavailabilityConflict) {
            return false;
        }

        // Check working hours for the day
        DayOfWeek dayOfWeek = startTime.getDayOfWeek();
        Optional<EmployeeWorkingHours> workingHoursOpt = 
                workingHoursRepository.findByEmployee_EmployeeIdAndDayOfWeek(employeeId, dayOfWeek);
        
        if (workingHoursOpt.isEmpty() || !workingHoursOpt.get().getIsWorkingDay()) {
            return false;
        }

        EmployeeWorkingHours wh = workingHoursOpt.get();
        if (!isWithinWorkingHours(wh, startTime.toLocalTime(), endTime.toLocalTime())) {
            return false;
        }

        // Check assignment conflicts with travel time buffer
        LocalDateTime bufferStartTime = startTime.minusMinutes(DEFAULT_TRAVEL_TIME_MINUTES);
        LocalDateTime bufferEndTime = endTime.plusMinutes(DEFAULT_TRAVEL_TIME_MINUTES);
        
        boolean hasAssignmentConflict = assignmentRepository.hasActiveAssignmentConflict(
                employeeId, bufferStartTime, bufferEndTime);
        
        return !hasAssignmentConflict;
    }

    /**
     * Get employees in a specific working zone
     */
    private List<Employee> getEmployeesInWorkingZone(String ward, String city) {
        String normalizedWard = normalizeLocationName(ward);
        String normalizedCity = normalizeLocationName(city);

        if (normalizedWard == null && normalizedCity == null) {
            return employeeRepository.findAll().stream()
                    .filter(emp -> emp.getEmployeeStatus() == EmployeeStatus.AVAILABLE)
                    .collect(Collectors.toList());
        }

        List<EmployeeWorkingZone> workingZones = workingZoneRepository.findByLocationContaining(normalizedWard, normalizedCity);
        return workingZones.stream()
                .map(EmployeeWorkingZone::getEmployee)
                .filter(emp -> emp.getEmployeeStatus() == EmployeeStatus.AVAILABLE)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get duration in minutes from service or request
     */
    private int getDurationMinutes(AvailableSlotsRequest request) {
        if (request.durationMinutes() != null && request.durationMinutes() > 0) {
            return request.durationMinutes();
        }

        if (request.serviceId() != null) {
            Optional<iuh.house_keeping_service_be.models.Service> serviceOpt = 
                    serviceRepository.findById(request.serviceId());
            if (serviceOpt.isPresent()) {
                BigDecimal hours = serviceOpt.get().getEstimatedDurationHours();
                if (hours != null) {
                    return hours.multiply(BigDecimal.valueOf(60)).intValue();
                }
            }
        }

        // Return default duration if not specified
        log.info("Using default service duration: {} minutes", DEFAULT_SERVICE_DURATION_MINUTES);
        return DEFAULT_SERVICE_DURATION_MINUTES;
    }

    /**
     * Normalize location name by removing prefixes
     */
    private String normalizeLocationName(String location) {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }

        String normalized = location.trim();
        normalized = normalized.replaceFirst("(?i)^P\\.\\s*", "");
        normalized = normalized.replaceFirst("(?i)^Phường\\s+", "");
        normalized = normalized.replaceFirst("(?i)^TP\\.\\s*", "");
        normalized = normalized.replaceFirst("(?i)^Thành phố\\s+", "");

        return normalized.trim();
    }

    /**
     * Get Vietnamese display name for day of week
     */
    private String getDayOfWeekDisplay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }

    /**
     * Fetch average rating for employee
     */
    private Double fetchAverageEmployeeRating(String employeeId) {
        try {
            return reviewDetailRepository.findAverageRatingByEmployeeId(employeeId);
        } catch (Exception e) {
            log.warn("Could not load average rating for employee {}: {}", employeeId, e.getMessage());
            return null;
        }
    }
}
