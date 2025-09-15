package iuh.house_keeping_service_be.services.EmployeeScheduleService.impl;

import iuh.house_keeping_service_be.dtos.EmployeeSchedule.*;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.EmployeeScheduleService.EmployeeScheduleService;
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

    @Override
    public ApiResponse<List<EmployeeScheduleResponse>> getAvailableEmployees(EmployeeScheduleRequest request) {
        try {
            List<Employee> employees = getEmployeesInWorkingZone(request.district(), request.city());

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
            List<Employee> employees = getEmployeesInWorkingZone(request.district(), request.city());

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
                    .createdAt(LocalDateTime.now())
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

    private List<Employee> getEmployeesInWorkingZone(String district, String city) {
        if (district == null && city == null) {
            return employeeRepository.findAll().stream()
                    .filter(emp -> emp.getEmployeeStatus() == EmployeeStatus.AVAILABLE)
                    .collect(Collectors.toList());
        }

        List<EmployeeWorkingZone> workingZones = workingZoneRepository.findByLocation(district, city);
        return workingZones.stream()
                .map(EmployeeWorkingZone::getEmployee)
                .filter(emp -> emp.getEmployeeStatus() == EmployeeStatus.AVAILABLE)
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
                .map(ewz -> new WorkingZone(ewz.getDistrict(), ewz.getCity()))
                .collect(Collectors.toList());

        String rating = employee.getRating() != null ? employee.getRating().toString() : "N/A";

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
            log.info("Finding suitable employees for service: {}, booking time: {}",
                    request.serviceId(), request.bookingTime());

            // 1. Lấy thông tin service để tính thời gian kết thúc
            Optional<iuh.house_keeping_service_be.models.Service> serviceOpt = serviceRepository.findById(request.serviceId());
            if (serviceOpt.isEmpty()) {
                return new ApiResponse<>(false, "Không tìm thấy dịch vụ với ID: " + request.serviceId(), null);
            }

            iuh.house_keeping_service_be.models.Service service = serviceOpt.get();

            // Tính thời gian kết thúc dựa trên estimated_duration_hours
            LocalDateTime startTime = request.bookingTime();

            if (startTime == null) {
                return new ApiResponse<>(false, "Thời gian đặt dịch vụ không hợp lệ", null);
            }
            if (startTime.isBefore(LocalDateTime.now())) {
                return new ApiResponse<>(false, "Thời gian đặt dịch vụ phải ở tương lai", null);
            }

            // Validate service duration
            BigDecimal estimatedDuration = service.getEstimatedDurationHours();
            if (estimatedDuration == null) {
                return new ApiResponse<>(false, "Dịch vụ không có thời lượng dự kiến", null);
            }

            LocalDateTime endTime = startTime.plusHours(estimatedDuration.longValue());

            log.info("Service duration: {} hours, calculated end time: {}",
                    estimatedDuration, endTime);

            // 2. Lọc nhân viên theo khu vực làm việc
            List<Employee> potentialEmployees = filterEmployeesByWorkingZone(request.district(), request.city());

            if (potentialEmployees.isEmpty()) {
                return new ApiResponse<>(true, "Không có nhân viên nào trong khu vực được chỉ định", Collections.emptyList());
            }

            log.info("Found {} potential employees in working zone", potentialEmployees.size());

            // 3. Kiểm tra tình trạng rảnh của từng nhân viên
            List<SuitableEmployeeResponse> availableEmployees = new ArrayList<>();

            for (Employee employee : potentialEmployees) {
                if (isEmployeeAvailable(employee.getEmployeeId(), startTime, endTime)) {
                    SuitableEmployeeResponse employeeResponse = buildSuitableEmployeeResponse(employee);
                    availableEmployees.add(employeeResponse);
                    log.debug("Employee {} is available", employee.getFullName());
                } else {
                    log.debug("Employee {} is busy during requested time", employee.getFullName());
                }
            }

            // 4. Sắp xếp theo rating giảm dần
            availableEmployees.sort((e1, e2) -> {
                if (e1.rating() == null && e2.rating() == null) return 0;
                if (e1.rating() == null) return 1;
                if (e2.rating() == null) return -1;
                return e2.rating().compareTo(e1.rating());
            });

            log.info("Found {} available employees, sorted by rating", availableEmployees.size());

            String message = availableEmployees.isEmpty()
                    ? "Không có nhân viên nào rảnh trong thời gian được yêu cầu"
                    : String.format("Tìm thấy %d nhân viên phù hợp cho dịch vụ %s",
                    availableEmployees.size(), service.getName());

            return new ApiResponse<>(true, message, availableEmployees);

        } catch (Exception e) {
            log.error("Error in findSuitableEmployees: ", e);
            return new ApiResponse<>(false, "Lỗi khi tìm kiếm nhân viên phù hợp: " + e.getMessage(), null);
        }
    }

    /**
     * Lọc nhân viên theo khu vực làm việc
     */
    private List<Employee> filterEmployeesByWorkingZone(String district, String city) {
        if (district != null && city != null) {
            // Sử dụng method hiện có để lọc theo location
            List<EmployeeWorkingZone> workingZones = workingZoneRepository.findByLocation(district, city);
            log.info("Filtering by district: {} and city: {}", district, city);

            return workingZones.stream()
                    .map(EmployeeWorkingZone::getEmployee)
                    .filter(emp -> emp.getEmployeeStatus() == EmployeeStatus.AVAILABLE)
                    .distinct()
                    .collect(Collectors.toList());

        } else if (city != null) {
            // Lọc chỉ theo city
            List<EmployeeWorkingZone> workingZones = workingZoneRepository.findByLocation(null, city);
            log.info("Filtering by city: {}", city);

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
     * Xây dựng response cho nhân viên phù hợp
     */
    private SuitableEmployeeResponse buildSuitableEmployeeResponse(Employee employee) {
        // Lấy thông tin khu vực làm việc từ employee's working zones
        List<EmployeeWorkingZone> workingZones = employee.getWorkingZones();

        String[] districts = workingZones.stream()
                .map(EmployeeWorkingZone::getDistrict)
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

        // Convert rating to string format
        String ratingStr = employee.getRating() != null ? employee.getRating().toString() : "N/A";

        return new SuitableEmployeeResponse(
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getAvatar(),
                employee.getSkills() == null ? Collections.emptyList() : employee.getSkills(),
                ratingStr,
                "AVAILABLE",
                districts,
                city,
                completedJobs
        );
    }
}