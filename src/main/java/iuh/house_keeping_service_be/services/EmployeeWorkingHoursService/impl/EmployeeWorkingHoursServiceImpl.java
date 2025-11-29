package iuh.house_keeping_service_be.services.EmployeeWorkingHoursService.impl;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.models.EmployeeWorkingHours;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.repositories.EmployeeWorkingHoursRepository;
import iuh.house_keeping_service_be.services.EmployeeWorkingHoursService.EmployeeWorkingHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeWorkingHoursServiceImpl implements EmployeeWorkingHoursService {

    private final EmployeeWorkingHoursRepository workingHoursRepository;
    private final EmployeeRepository employeeRepository;

    // Default working hours
    private static final LocalTime DEFAULT_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_END_TIME = LocalTime.of(18, 0);
    private static final LocalTime DEFAULT_BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime DEFAULT_BREAK_END = LocalTime.of(13, 0);

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<WorkingHoursResponse>> getEmployeeWorkingHours(String employeeId) {
        try {
            // Check if employee exists
            if (!employeeRepository.existsById(employeeId)) {
                return new ApiResponse<>(false, "Không tìm thấy nhân viên", null);
            }

            List<EmployeeWorkingHours> workingHours = workingHoursRepository.findByEmployee_EmployeeId(employeeId);
            
            // If no working hours configured, return empty with message
            if (workingHours.isEmpty()) {
                return new ApiResponse<>(true, "Nhân viên chưa cài đặt khung giờ làm việc", Collections.emptyList());
            }

            List<WorkingHoursResponse> responses = workingHours.stream()
                    .sorted(Comparator.comparing(EmployeeWorkingHours::getDayOfWeek))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "Lấy khung giờ làm việc thành công", responses);
        } catch (Exception e) {
            log.error("Error getting employee working hours: ", e);
            return new ApiResponse<>(false, "Lỗi khi lấy khung giờ làm việc: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<WorkingHoursResponse> setWorkingHours(WorkingHoursRequest request) {
        try {
            // Validate request
            ApiResponse<Void> validation = validateWorkingHoursRequest(request);
            if (!validation.success()) {
                return new ApiResponse<>(false, validation.message(), null);
            }

            // Check if employee exists
            Optional<Employee> employeeOpt = employeeRepository.findById(request.employeeId());
            if (employeeOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy nhân viên", null);
            }

            // Check if working hours already exist for this day
            Optional<EmployeeWorkingHours> existingOpt = workingHoursRepository
                    .findByEmployee_EmployeeIdAndDayOfWeek(request.employeeId(), request.dayOfWeek());

            EmployeeWorkingHours workingHours;
            if (existingOpt.isPresent()) {
                // Update existing
                workingHours = existingOpt.get();
                updateWorkingHoursFromRequest(workingHours, request);
            } else {
                // Create new
                workingHours = EmployeeWorkingHours.builder()
                        .employee(employeeOpt.get())
                        .dayOfWeek(request.dayOfWeek())
                        .startTime(request.startTime())
                        .endTime(request.endTime())
                        .isWorkingDay(request.isWorkingDay() != null ? request.isWorkingDay() : true)
                        .breakStartTime(request.breakStartTime())
                        .breakEndTime(request.breakEndTime())
                        .build();
            }

            workingHours = workingHoursRepository.save(workingHours);

            return new ApiResponse<>(true, "Cập nhật khung giờ làm việc thành công", mapToResponse(workingHours));
        } catch (Exception e) {
            log.error("Error setting working hours: ", e);
            return new ApiResponse<>(false, "Lỗi khi cập nhật khung giờ làm việc: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<WorkingHoursResponse>> setWeeklyWorkingHours(WorkingHoursBatchRequest request) {
        try {
            // Check if employee exists
            Optional<Employee> employeeOpt = employeeRepository.findById(request.employeeId());
            if (employeeOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy nhân viên", null);
            }

            if (request.weeklySchedule() == null || request.weeklySchedule().isEmpty()) {
                return new ApiResponse<>(false, "Danh sách lịch làm việc không được rỗng", null);
            }

            List<EmployeeWorkingHours> savedWorkingHours = new ArrayList<>();

            for (WorkingHoursBatchRequest.DayWorkingHours daySchedule : request.weeklySchedule()) {
                // Validate each day's schedule
                if (daySchedule.dayOfWeek() == null) {
                    continue;
                }

                if (daySchedule.isWorkingDay() != null && daySchedule.isWorkingDay()) {
                    if (daySchedule.startTime() == null || daySchedule.endTime() == null) {
                        return new ApiResponse<>(false, 
                                "Ngày làm việc " + daySchedule.dayOfWeek() + " phải có giờ bắt đầu và kết thúc", null);
                    }
                    if (!daySchedule.endTime().isAfter(daySchedule.startTime())) {
                        return new ApiResponse<>(false,
                                "Giờ kết thúc phải sau giờ bắt đầu cho ngày " + daySchedule.dayOfWeek(), null);
                    }
                }

                // Find or create working hours for this day
                Optional<EmployeeWorkingHours> existingOpt = workingHoursRepository
                        .findByEmployee_EmployeeIdAndDayOfWeek(request.employeeId(), daySchedule.dayOfWeek());

                EmployeeWorkingHours workingHours;
                if (existingOpt.isPresent()) {
                    workingHours = existingOpt.get();
                } else {
                    workingHours = new EmployeeWorkingHours();
                    workingHours.setEmployee(employeeOpt.get());
                    workingHours.setDayOfWeek(daySchedule.dayOfWeek());
                }

                workingHours.setStartTime(daySchedule.startTime() != null ? daySchedule.startTime() : DEFAULT_START_TIME);
                workingHours.setEndTime(daySchedule.endTime() != null ? daySchedule.endTime() : DEFAULT_END_TIME);
                workingHours.setIsWorkingDay(daySchedule.isWorkingDay() != null ? daySchedule.isWorkingDay() : true);
                workingHours.setBreakStartTime(daySchedule.breakStartTime());
                workingHours.setBreakEndTime(daySchedule.breakEndTime());

                savedWorkingHours.add(workingHoursRepository.save(workingHours));
            }

            List<WorkingHoursResponse> responses = savedWorkingHours.stream()
                    .sorted(Comparator.comparing(EmployeeWorkingHours::getDayOfWeek))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "Cập nhật lịch làm việc tuần thành công", responses);
        } catch (Exception e) {
            log.error("Error setting weekly working hours: ", e);
            return new ApiResponse<>(false, "Lỗi khi cập nhật lịch làm việc tuần: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<WorkingHoursResponse> updateWorkingHours(String workingHoursId, WorkingHoursRequest request) {
        try {
            Optional<EmployeeWorkingHours> existingOpt = workingHoursRepository.findById(workingHoursId);
            if (existingOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy khung giờ làm việc", null);
            }

            // Validate request
            ApiResponse<Void> validation = validateWorkingHoursRequest(request);
            if (!validation.success()) {
                return new ApiResponse<>(false, validation.message(), null);
            }

            EmployeeWorkingHours workingHours = existingOpt.get();
            updateWorkingHoursFromRequest(workingHours, request);
            workingHours = workingHoursRepository.save(workingHours);

            return new ApiResponse<>(true, "Cập nhật khung giờ làm việc thành công", mapToResponse(workingHours));
        } catch (Exception e) {
            log.error("Error updating working hours: ", e);
            return new ApiResponse<>(false, "Lỗi khi cập nhật khung giờ làm việc: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> deleteWorkingHours(String workingHoursId) {
        try {
            if (!workingHoursRepository.existsById(workingHoursId)) {
                return new ApiResponse<>(false, "Không tìm thấy khung giờ làm việc", null);
            }

            workingHoursRepository.deleteById(workingHoursId);
            return new ApiResponse<>(true, "Xóa khung giờ làm việc thành công", null);
        } catch (Exception e) {
            log.error("Error deleting working hours: ", e);
            return new ApiResponse<>(false, "Lỗi khi xóa khung giờ làm việc: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<WorkingHoursResponse>> initializeDefaultWorkingHours(String employeeId) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy nhân viên", null);
            }

            // Check if working hours already exist
            if (workingHoursRepository.existsByEmployee_EmployeeId(employeeId)) {
                return new ApiResponse<>(false, "Nhân viên đã có khung giờ làm việc được cài đặt", null);
            }

            Employee employee = employeeOpt.get();
            List<EmployeeWorkingHours> workingHoursList = new ArrayList<>();

            for (DayOfWeek day : DayOfWeek.values()) {
                boolean isWorkingDay = day != DayOfWeek.SUNDAY; // Default: Sunday is off

                EmployeeWorkingHours workingHours = EmployeeWorkingHours.builder()
                        .employee(employee)
                        .dayOfWeek(day)
                        .startTime(DEFAULT_START_TIME)
                        .endTime(DEFAULT_END_TIME)
                        .isWorkingDay(isWorkingDay)
                        .breakStartTime(DEFAULT_BREAK_START)
                        .breakEndTime(DEFAULT_BREAK_END)
                        .build();

                workingHoursList.add(workingHoursRepository.save(workingHours));
            }

            List<WorkingHoursResponse> responses = workingHoursList.stream()
                    .sorted(Comparator.comparing(EmployeeWorkingHours::getDayOfWeek))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "Khởi tạo khung giờ làm việc mặc định thành công", responses);
        } catch (Exception e) {
            log.error("Error initializing default working hours: ", e);
            return new ApiResponse<>(false, "Lỗi khi khởi tạo khung giờ làm việc: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<WorkingHoursResponse> copyWorkingHours(String employeeId, DayOfWeek sourceDay, DayOfWeek targetDay) {
        try {
            Optional<EmployeeWorkingHours> sourceOpt = workingHoursRepository
                    .findByEmployee_EmployeeIdAndDayOfWeek(employeeId, sourceDay);

            if (sourceOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy khung giờ làm việc ngày nguồn", null);
            }

            EmployeeWorkingHours source = sourceOpt.get();
            
            Optional<EmployeeWorkingHours> targetOpt = workingHoursRepository
                    .findByEmployee_EmployeeIdAndDayOfWeek(employeeId, targetDay);

            EmployeeWorkingHours target;
            if (targetOpt.isPresent()) {
                target = targetOpt.get();
            } else {
                target = new EmployeeWorkingHours();
                target.setEmployee(source.getEmployee());
                target.setDayOfWeek(targetDay);
            }

            target.setStartTime(source.getStartTime());
            target.setEndTime(source.getEndTime());
            target.setIsWorkingDay(source.getIsWorkingDay());
            target.setBreakStartTime(source.getBreakStartTime());
            target.setBreakEndTime(source.getBreakEndTime());

            target = workingHoursRepository.save(target);

            return new ApiResponse<>(true, "Sao chép khung giờ làm việc thành công", mapToResponse(target));
        } catch (Exception e) {
            log.error("Error copying working hours: ", e);
            return new ApiResponse<>(false, "Lỗi khi sao chép khung giờ làm việc: " + e.getMessage(), null);
        }
    }

    /**
     * Validate working hours request
     */
    private ApiResponse<Void> validateWorkingHoursRequest(WorkingHoursRequest request) {
        if (request.employeeId() == null || request.employeeId().isBlank()) {
            return new ApiResponse<>(false, "ID nhân viên không được để trống", null);
        }

        if (request.dayOfWeek() == null) {
            return new ApiResponse<>(false, "Ngày trong tuần không được để trống", null);
        }

        // If it's a working day, start and end times are required
        if (request.isWorkingDay() == null || request.isWorkingDay()) {
            if (request.startTime() == null || request.endTime() == null) {
                return new ApiResponse<>(false, "Giờ bắt đầu và kết thúc không được để trống cho ngày làm việc", null);
            }

            if (!request.endTime().isAfter(request.startTime())) {
                return new ApiResponse<>(false, "Giờ kết thúc phải sau giờ bắt đầu", null);
            }

            // Validate break times if provided
            if (request.breakStartTime() != null || request.breakEndTime() != null) {
                if (request.breakStartTime() == null || request.breakEndTime() == null) {
                    return new ApiResponse<>(false, "Cả giờ bắt đầu và kết thúc nghỉ trưa đều phải được cung cấp", null);
                }

                if (!request.breakEndTime().isAfter(request.breakStartTime())) {
                    return new ApiResponse<>(false, "Giờ kết thúc nghỉ trưa phải sau giờ bắt đầu", null);
                }

                if (request.breakStartTime().isBefore(request.startTime()) || 
                    request.breakEndTime().isAfter(request.endTime())) {
                    return new ApiResponse<>(false, "Giờ nghỉ trưa phải nằm trong khung giờ làm việc", null);
                }
            }
        }

        return new ApiResponse<>(true, "Valid", null);
    }

    /**
     * Update working hours entity from request
     */
    private void updateWorkingHoursFromRequest(EmployeeWorkingHours workingHours, WorkingHoursRequest request) {
        if (request.startTime() != null) {
            workingHours.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            workingHours.setEndTime(request.endTime());
        }
        if (request.isWorkingDay() != null) {
            workingHours.setIsWorkingDay(request.isWorkingDay());
        }
        workingHours.setBreakStartTime(request.breakStartTime());
        workingHours.setBreakEndTime(request.breakEndTime());
    }

    /**
     * Map entity to response DTO
     */
    private WorkingHoursResponse mapToResponse(EmployeeWorkingHours workingHours) {
        return new WorkingHoursResponse(
                workingHours.getWorkingHoursId(),
                workingHours.getDayOfWeek(),
                WorkingHoursResponse.getDayOfWeekDisplay(workingHours.getDayOfWeek()),
                workingHours.getStartTime(),
                workingHours.getEndTime(),
                workingHours.getIsWorkingDay(),
                workingHours.getBreakStartTime(),
                workingHours.getBreakEndTime()
        );
    }
}
