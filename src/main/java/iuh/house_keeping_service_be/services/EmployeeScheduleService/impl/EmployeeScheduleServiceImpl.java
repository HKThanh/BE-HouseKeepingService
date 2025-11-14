package iuh.house_keeping_service_be.services.EmployeeScheduleService.impl;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.EmployeeScheduleService.EmployeeScheduleService;
import iuh.house_keeping_service_be.services.RecommendationService.EmployeeRecommendationService;
import iuh.house_keeping_service_be.enums.Rating;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeScheduleServiceImpl implements EmployeeScheduleService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeUnavailabilityRepository unavailabilityRepository;
    private final AssignmentRepository assignmentRepository;
    private final EmployeeWorkingZoneRepository workingZoneRepository;
    private final ServiceRepository serviceRepository;
    private final EmployeeRecommendationService recommendationService;
    private final ReviewDetailRepository reviewDetailRepository;

    @Override
    public ApiResponse<List<EmployeeScheduleResponse>> getAvailableEmployees(EmployeeScheduleRequest request) {
        try {
            List<Employee> employees = getEmployeesInWorkingZone(request.ward(), request.city());

            if (employees.isEmpty()) {
                return new ApiResponse<>(true, "Không tìm thấy nhân viên ở " + request.city(), Collections.emptyList());
            }

            List<EmployeeScheduleResponse> availableEmployees = employees.stream()
                    .filter(employee -> isEmployeeAvailable(employee.getEmployeeId(), request.startDate(), request.endDate()))
                    .map(employee -> mapToEmployeeScheduleResponse(employee, request.startDate(), request.endDate(), true))
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "Lấy danh sách nhân viên rảnh thành công", availableEmployees);
        } catch (Exception e) {
            log.error("Error getting available employees: ", e);
            return new ApiResponse<>(false, "Lỗi khi lấy danh sách nhân viên rảnh: " + e.getMessage(), Collections.emptyList());
        }
    }

    @Override
    public ApiResponse<List<EmployeeScheduleResponse>> getBusyEmployees(EmployeeScheduleRequest request) {
        try {
            List<Employee> employees = getEmployeesInWorkingZone(request.ward(), request.city());

            if (employees.isEmpty()) {
                return new ApiResponse<>(true, "Không tìm thấy nhân viên nào ở " + request.city(), Collections.emptyList());
            }

            List<EmployeeScheduleResponse> busyEmployees = employees.stream()
                    .filter(employee -> !isEmployeeAvailable(employee.getEmployeeId(), request.startDate(), request.endDate()))
                    .map(employee -> mapToEmployeeScheduleResponse(employee, request.startDate(), request.endDate(), false))
                    .collect(Collectors.toList());

            return new ApiResponse<>(true, "Lấy danh sách nhân viên bận thành công", busyEmployees);
        } catch (Exception e) {
            log.error("Error getting busy employees: ", e);
            return new ApiResponse<>(false, "Lỗi khi lấy danh sách nhân viên bận: " + e.getMessage(), Collections.emptyList());
        }
    }

    @Override
    public ApiResponse<EmployeeScheduleResponse> getEmployeeSchedule(String employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy nhân viên này", null);
            }

            Employee employee = employeeOpt.get();
            boolean isAvailable = isEmployeeAvailable(employeeId, startDate, endDate);
            EmployeeScheduleResponse response = mapToEmployeeScheduleResponse(employee, startDate, endDate, isAvailable);

            return new ApiResponse<>(true, "Lấy lịch làm việc nhân viên thành công", response);
        } catch (Exception e) {
            log.error("Error getting employee schedule: ", e);
            return new ApiResponse<>(false, "Lỗi khi lấy lịch làm việc của nhân viên: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<EmployeeScheduleResponse> createUnavailability(UnavailabilityRequest request) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(request.employeeId());
            if (employeeOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy nhân viên này", null);
            }

            if (hasScheduleConflict(request.employeeId(), request.startTime(), request.endTime())) {
                return new ApiResponse<>(false, "Đã bị trùng lịch ", null);
            }

            EmployeeUnavailability unavailability = EmployeeUnavailability.builder()
                    .employee(employeeOpt.get())
                    .startTime(request.startTime())
                    .endTime(request.endTime())
                    .reason(request.reason())
                    .isApproved(true)
                    .build();

            unavailabilityRepository.save(unavailability);

            EmployeeScheduleResponse response = mapToEmployeeScheduleResponse(
                    employeeOpt.get(), request.startTime(), request.endTime(), false);

            return new ApiResponse<>(true, "Tạo ngày nghỉ thành công", response);
        } catch (Exception e) {
            log.error("Error creating unavailability: ", e);
            return new ApiResponse<>(false, "Lỗi khi tạo ngày nghỉ: " + e.getMessage(), null);
        }
    }

    private List<Employee> getEmployeesInWorkingZone(String ward, String city) {
        // Chuẩn hóa tên phường và thành phố
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

    private boolean isEmployeeAvailable(String employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean hasUnavailabilityConflict = unavailabilityRepository.hasConflict(employeeId, startTime, endTime);
        boolean hasAssignmentConflict = assignmentRepository.hasActiveAssignmentConflict(employeeId, startTime, endTime);
        return !hasUnavailabilityConflict && !hasAssignmentConflict;
    }

    private boolean hasScheduleConflict(String employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        return unavailabilityRepository.hasConflict(employeeId, startTime, endTime) ||
                assignmentRepository.hasActiveAssignmentConflict(employeeId, startTime, endTime);
    }

    private EmployeeScheduleResponse mapToEmployeeScheduleResponse(Employee employee, LocalDateTime startDate, LocalDateTime endDate, boolean isAvailable) {
        List<String> skills = employee.getSkills() != null ? employee.getSkills() : Collections.emptyList();

        List<WorkingZone> workingZones = employee.getWorkingZones().stream()
                .map(ewz -> new WorkingZone(ewz.getWard(), ewz.getCity()))
                .collect(Collectors.toList());

        Double averageRating = fetchAverageEmployeeRating(employee.getEmployeeId());
        String rating = formatRatingString(averageRating, employee.getRating());

        List<TimeSlot> timeSlots = new ArrayList<>();

        if (isAvailable) {
            timeSlots.addAll(getAvailableTimeSlots(employee.getEmployeeId(), startDate, endDate));
        } else {
            timeSlots.addAll(getBusyTimeSlots(employee.getEmployeeId(), startDate, endDate));
        }

        return new EmployeeScheduleResponse(
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getAvatar(),
                skills,
                rating,
                employee.getEmployeeStatus().toString(),
                workingZones,
                timeSlots
        );
    }

    private List<TimeSlot> getAvailableTimeSlots(String employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        List<TimeSlot> busySlots = getBusyTimeSlots(employeeId, startDate, endDate);
        List<TimeSlot> availableSlots = new ArrayList<>();

        if (busySlots.isEmpty()) {
            availableSlots.add(new TimeSlot(
                    startDate, endDate, "AVAILABLE", null, null, null, null, null, null,
                    (int) java.time.Duration.between(startDate, endDate).toHours()
            ));
        }

        return availableSlots;
    }

    private List<TimeSlot> getBusyTimeSlots(String employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        List<TimeSlot> busySlots = new ArrayList<>();

        // Get unavailability periods
        List<EmployeeUnavailability> unavailabilities = unavailabilityRepository
                .findByEmployeeAndPeriod(employeeId, startDate, endDate);

        unavailabilities.forEach(unavail -> busySlots.add(new TimeSlot(
                unavail.getStartTime(),
                unavail.getEndTime(),
                "UNAVAILABLE",
                unavail.getReason(),
                null, null, null, null, null,
                (int) java.time.Duration.between(unavail.getStartTime(), unavail.getEndTime()).toHours()
        )));

        // Get assignments
        List<Assignment> assignments = assignmentRepository.findByEmployeeAndPeriodWithStatuses(
                employeeId, startDate, endDate,
                Arrays.asList(AssignmentStatus.ASSIGNED, AssignmentStatus.IN_PROGRESS)
        );

        assignments.forEach(assignment -> {
            BookingDetail bd = assignment.getBookingDetail();
            Booking booking = bd.getBooking();
            iuh.house_keeping_service_be.models.Service service = bd.getService();

            // Fix: Use the correct field name and convert BigDecimal to long for hours
            long durationHours = service.getEstimatedDurationHours() != null ?
                    service.getEstimatedDurationHours().longValue() : 1L;
            LocalDateTime assignmentEndTime = booking.getBookingTime().plusHours(durationHours);

            busySlots.add(new TimeSlot(
                    booking.getBookingTime(),
                    assignmentEndTime,
                    "ASSIGNMENT",
                    null,
                    booking.getBookingCode(),
                    service.getName(),
                    booking.getCustomer().getFullName(),
                    booking.getAddress().getFullAddress(),
                    assignment.getStatus().toString(),
                    (int) durationHours
            ));
        });

        return busySlots.stream()
                .sorted(Comparator.comparing(TimeSlot::startTime))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SuitableEmployeeResponse>> findSuitableEmployees(SuitableEmployeeRequest request) {
        try {
            if (request.bookingTimes() != null && !request.bookingTimes().isEmpty()) {
                log.info("Finding suitable employees for service: {}, checking {} booking time slots",
                        request.serviceId(), request.bookingTimes().size());
            } else {
                log.info("Finding suitable employees for service: {}, booking time: {}",
                        request.serviceId(), request.bookingTime());
            }

            // 1. Lấy thông tin service để tính thời gian kết thúc
            Optional<iuh.house_keeping_service_be.models.Service> serviceOpt = serviceRepository.findById(request.serviceId());
            if (serviceOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy dịch vụ với ID: " + request.serviceId(), null);
            }

            iuh.house_keeping_service_be.models.Service service = serviceOpt.get();

            // Validate service duration
            BigDecimal estimatedDuration = service.getEstimatedDurationHours();
            if (estimatedDuration == null) {
                return new ApiResponse<>(false, "Dịch vụ không có thời lượng dự kiến", null);
            }

            // Validate booking time(s)
            boolean hasBookingTimes = request.bookingTimes() != null && !request.bookingTimes().isEmpty();
            LocalDateTime startTime = request.bookingTime();
            LocalDateTime endTime = null;

            // Nếu không có bookingTimes, bắt buộc phải có bookingTime
            if (!hasBookingTimes) {
                if (startTime == null) {
                    return new ApiResponse<>(false, "Thời gian đặt dịch vụ không hợp lệ (bookingTime hoặc bookingTimes phải được cung cấp)", null);
                }
                if (startTime.isBefore(LocalDateTime.now())) {
                    return new ApiResponse<>(false, "Thời gian đặt dịch vụ phải ở tương lai", null);
                }
                endTime = startTime.plusHours(estimatedDuration.longValue());
            } else {
                // Nếu có bookingTimes, validate tất cả các time slots
                for (LocalDateTime bookingTime : request.bookingTimes()) {
                    if (bookingTime == null) {
                        return new ApiResponse<>(false, "Danh sách bookingTimes chứa giá trị null", null);
                    }
                    if (bookingTime.isBefore(LocalDateTime.now())) {
                        return new ApiResponse<>(false, "Tất cả thời gian trong bookingTimes phải ở tương lai", null);
                    }
                }
            }

            log.info("Service duration: {} hours, calculated end time: {}",
                    estimatedDuration, endTime);

            // 2. Lọc nhân viên theo khu vực làm việc
            List<Employee> potentialEmployees = filterEmployeesByWorkingZone(request.ward(), request.city());

            if (potentialEmployees.isEmpty()) {
                return new ApiResponse<>(true, "Không có nhân viên nào trong khu vực được chỉ định", Collections.emptyList());
            }

            log.info("Found {} potential employees in working zone", potentialEmployees.size());

            // 3. Lấy danh sách nhân viên đã từng phục vụ customer (nếu có customerId)
            Set<String> employeesWorkedWithCustomer = new HashSet<>();
            if (request.customerId() != null && !request.customerId().isBlank()) {
                try {
                    List<String> workedEmployeeIds = assignmentRepository
                            .findEmployeeIdsByCustomerWithCompletedBookings(request.customerId());
                    employeesWorkedWithCustomer.addAll(workedEmployeeIds);
                    log.info("Found {} employees who have worked with customer {}", 
                            employeesWorkedWithCustomer.size(), request.customerId());
                } catch (Exception e) {
                    log.warn("Could not fetch employees worked with customer: {}", e.getMessage());
                }
            }

            // 4. Kiểm tra tình trạng rảnh và phân loại nhân viên
            List<SuitableEmployeeResponse> employeesWithHistory = new ArrayList<>();
            List<SuitableEmployeeResponse> employeesWithoutHistory = new ArrayList<>();

            for (Employee employee : potentialEmployees) {
                boolean isAvailable = false;
                
                // Nếu có danh sách bookingTimes, kiểm tra tất cả các slot
                if (request.bookingTimes() != null && !request.bookingTimes().isEmpty()) {
                    isAvailable = true;
                    for (LocalDateTime bookingTime : request.bookingTimes()) {
                        LocalDateTime slotEndTime = bookingTime.plusHours(estimatedDuration.longValue());
                        if (!isEmployeeAvailable(employee.getEmployeeId(), bookingTime, slotEndTime)) {
                            isAvailable = false;
                            log.debug("Employee {} is busy at time slot: {}", employee.getFullName(), bookingTime);
                            break;
                        }
                    }
                } else {
                    // Nếu không có bookingTimes, kiểm tra theo bookingTime đơn lẻ (logic cũ)
                    isAvailable = isEmployeeAvailable(employee.getEmployeeId(), startTime, endTime);
                }
                
                if (isAvailable) {
                    boolean hasWorkedWithCustomer = employeesWorkedWithCustomer.contains(employee.getEmployeeId());
                    SuitableEmployeeResponse employeeResponse = buildSuitableEmployeeResponse(employee, hasWorkedWithCustomer);
                    
                    if (hasWorkedWithCustomer) {
                        employeesWithHistory.add(employeeResponse);
                        log.debug("Employee {} is available and has worked with customer", employee.getFullName());
                    } else {
                        employeesWithoutHistory.add(employeeResponse);
                        log.debug("Employee {} is available (new to customer)", employee.getFullName());
                    }
                } else {
                    log.debug("Employee {} is busy during requested time", employee.getFullName());
                }
            }

            // 5. Áp dụng machine learning cho từng nhóm riêng biệt
            if (!employeesWithHistory.isEmpty()) {
                employeesWithHistory = recommendationService.recommend(request, employeesWithHistory);
                log.info("Ranked {} employees with customer history using ML", employeesWithHistory.size());
            }
            
            if (!employeesWithoutHistory.isEmpty()) {
                employeesWithoutHistory = recommendationService.recommend(request, employeesWithoutHistory);
                log.info("Ranked {} new employees using ML", employeesWithoutHistory.size());
            }

            // 6. Kết hợp: Nhân viên đã từng phục vụ lên trước, sau đó đến nhân viên mới
            List<SuitableEmployeeResponse> availableEmployees = new ArrayList<>();
            availableEmployees.addAll(employeesWithHistory);
            availableEmployees.addAll(employeesWithoutHistory);

            log.info("Found {} available employees ({} with history, {} new) after ML ranking", 
                    availableEmployees.size(), employeesWithHistory.size(), employeesWithoutHistory.size());

            String message;
            if (availableEmployees.isEmpty()) {
                message = "Không có nhân viên nào rảnh trong thời gian được yêu cầu";
            } else {
                if (employeesWithHistory.isEmpty() && employeesWithoutHistory.isEmpty()) {
                    message = String.format("Tìm thấy %d nhân viên phù hợp cho dịch vụ %s",
                            availableEmployees.size(), service.getName());
                } else if (employeesWithHistory.isEmpty()) {
                    message = String.format("Tìm thấy %d nhân viên phù hợp cho dịch vụ %s",
                            employeesWithoutHistory.size(), service.getName());
                } else if (employeesWithoutHistory.isEmpty()) {
                    message = String.format("Tìm thấy %d nhân viên đã từng phục vụ bạn cho dịch vụ %s",
                            employeesWithHistory.size(), service.getName());
                } else {
                    message = String.format("Tìm thấy %d nhân viên phù hợp cho dịch vụ %s (%d đã từng phục vụ bạn, %d nhân viên khác)",
                            availableEmployees.size(), service.getName(), 
                            employeesWithHistory.size(), employeesWithoutHistory.size());
                }
            }

            return new ApiResponse<>(true, message, availableEmployees);

        } catch (Exception e) {
            log.error("Error in findSuitableEmployees: ", e);
            return new ApiResponse<>(false, "Lỗi khi tìm kiếm nhân viên phù hợp: " + e.getMessage(), null);
        }
    }

    /**
     * Lọc nhân viên theo khu vực làm việc
     */
    private List<Employee> filterEmployeesByWorkingZone(String ward, String city) {
        // Chuẩn hóa tên phường và thành phố
        String normalizedWard = normalizeLocationName(ward);
        String normalizedCity = normalizeLocationName(city);
        
        if (normalizedWard != null && normalizedCity != null) {
            // Sử dụng method LIKE để tìm kiếm theo chuỗi con
            List<EmployeeWorkingZone> workingZones = workingZoneRepository.findByLocationContaining(normalizedWard, normalizedCity);
            log.info("Filtering by ward: {} (normalized from: {}) and city: {} (normalized from: {})", 
                    normalizedWard, ward, normalizedCity, city);

            return workingZones.stream()
                    .map(EmployeeWorkingZone::getEmployee)
                    .filter(emp -> emp.getEmployeeStatus() == EmployeeStatus.AVAILABLE)
                    .distinct()
                    .collect(Collectors.toList());

        } else if (normalizedCity != null) {
            // Lọc chỉ theo city
            List<EmployeeWorkingZone> workingZones = workingZoneRepository.findByLocationContaining(null, normalizedCity);
            log.info("Filtering by city: {} (normalized from: {})", normalizedCity, city);

            return workingZones.stream()
                    .map(EmployeeWorkingZone::getEmployee)
                    .filter(emp -> emp.getEmployeeStatus() == EmployeeStatus.AVAILABLE)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            // Lấy tất cả nhân viên đang hoạt động
            log.info("No location filter specified, getting all available employees");
            return employeeRepository.findAll().stream()
                    .filter(emp -> emp.getEmployeeStatus() == EmployeeStatus.AVAILABLE)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Chuẩn hóa tên địa điểm bằng cách loại bỏ các tiền tố
     * VD: "P. Tây Thạnh" -> "Tây Thạnh"
     *     "Phường Tây Thạnh" -> "Tây Thạnh"
     *     "TP. Hồ Chí Minh" -> "Hồ Chí Minh"
     *     "Thành phố Hồ Chí Minh" -> "Hồ Chí Minh"
     */
    private String normalizeLocationName(String location) {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }
        
        String normalized = location.trim();
        
        // Loại bỏ các tiền tố phường
        normalized = normalized.replaceFirst("(?i)^P\\.\\s*", "");
        normalized = normalized.replaceFirst("(?i)^Phường\\s+", "");
        normalized = normalized.replaceFirst("(?i)^phường\\s+", "");
        
        // Loại bỏ các tiền tố thành phố
        normalized = normalized.replaceFirst("(?i)^TP\\.\\s*", "");
        normalized = normalized.replaceFirst("(?i)^Tp\\.\\s*", "");
        normalized = normalized.replaceFirst("(?i)^Thành phố\\s+", "");
        normalized = normalized.replaceFirst("(?i)^thành phố\\s+", "");
        
        return normalized.trim();
    }

    /**
     * Xây dựng response cho nhân viên phù hợp
     */
    private SuitableEmployeeResponse buildSuitableEmployeeResponse(Employee employee, boolean hasWorkedWithCustomer) {
        // Lấy thông tin khu vực làm việc từ employee's working zones
        List<EmployeeWorkingZone> workingZones = employee.getWorkingZones();

        String[] wards = workingZones.stream()
                .map(EmployeeWorkingZone::getWard)
                .distinct()
                .toArray(String[]::new);

        String city = workingZones.isEmpty() ? null :
                workingZones.get(0).getCity();

        // Đếm số công việc đã hoàn thành - sử dụng method hiện có
        Integer completedJobs = 0; // Default value
        try {
            completedJobs = assignmentRepository.countCompletedJobsByEmployee(employee.getEmployeeId());
            if (completedJobs == null) completedJobs = 0;
        } catch (Exception e) {
            log.warn("Could not get completed jobs count for employee {}: {}", employee.getEmployeeId(), e.getMessage());
        }

        Double averageRating = fetchAverageEmployeeRating(employee.getEmployeeId());
        String ratingStr = formatRatingString(averageRating, employee.getRating());

        return new SuitableEmployeeResponse(
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getAvatar(),
                employee.getSkills() == null ? Collections.emptyList() : employee.getSkills(),
                ratingStr,
                "AVAILABLE",
                wards,
                city,
                completedJobs,
                hasWorkedWithCustomer,
                null
        );
    }

    private String formatRatingString(Double averageRating, Rating tier) {
        if (averageRating != null && averageRating > 0) {
            return String.format(Locale.US, "%.2f", averageRating);
        }

        return "0.00";
    }

    private Double fetchAverageEmployeeRating(String employeeId) {
        try {
            return reviewDetailRepository.findAverageRatingByEmployeeId(employeeId);
        } catch (Exception e) {
            log.warn("Could not load average rating for employee {}: {}", employeeId, e.getMessage());
            return null;
        }
    }

    private String translateRatingTier(Rating rating) {
        if (rating == null) {
            return "Đang cập nhật";
        }

        return switch (rating) {
            case HIGHEST -> "Xuất sắc";
            case HIGH -> "Tốt";
            case MEDIUM -> "Khá";
            case LOW -> "Trung bình";
            case LOWEST -> "Cần cải thiện";
        };
    }
}
