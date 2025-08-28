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
                return new ApiResponse<>(true, "Không tìm thấy nhân viên nào ở " +  request.city(), Collections.emptyList());
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
}