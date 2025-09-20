package iuh.house_keeping_service_be.services.AssignmentService.impl;

import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCancelRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.dtos.Assignment.response.BookingSummary;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.AssignmentService.AssignmentService;
//import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeUnavailabilityRepository employeeUnavailabilityRepository;
//    private final NotificationService notificationService;

    @Override
    public List<AssignmentDetailResponse> getEmployeeAssignments(String employeeId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Assignment> assignments;

        if (status != null && !status.isEmpty()) {
            try {
                AssignmentStatus assignmentStatus = AssignmentStatus.valueOf(status.toUpperCase());
                assignments = assignmentRepository.findByEmployeeIdAndStatusWithDetails(employeeId, assignmentStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}", status);
                assignments = assignmentRepository.findByEmployeeIdWithDetails(employeeId, pageable);
            }
        } else {
            assignments = assignmentRepository.findByEmployeeIdWithDetails(employeeId, pageable);
        }

        return assignments.stream()
                .map(this::mapToAssignmentDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingSummary> getAvailableBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "bookingTime"));
        List<Booking> bookings = bookingRepository.findAwaitingEmployeeBookings(pageable);

        return bookings.stream()
                .flatMap(b -> b.getBookingDetails().stream()
                        .filter(bd -> bd.getAssignments().isEmpty())
                        .map(bd -> new BookingSummary(
                                bd.getId(),
                                b.getBookingCode(),
                                bd.getService().getName(),
                                b.getAddress().getFullAddress(),
                                b.getBookingTime(),
                                bd.getService().getEstimatedDurationHours(),
                                bd.getQuantity()
                        )))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssignmentDetailResponse acceptBookingDetail(String detailId, String employeeId) {
        BookingDetail bookingDetail = bookingDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ"));

        Booking booking = bookingDetail.getBooking();
        if (booking == null) {
            throw new IllegalStateException("Không thể xác định booking của chi tiết dịch vụ này");
        }

        EnumSet<BookingStatus> allowedStatuses = EnumSet.of(BookingStatus.AWAITING_EMPLOYEE, BookingStatus.CONFIRMED);
        if (!allowedStatuses.contains(booking.getStatus())) {
            throw new IllegalStateException(String.format(
                    "Không thể nhận booking khi đang ở trạng thái %s", booking.getStatus().name()));
        }

        LocalDateTime shiftStart = bookingDetail.getBooking().getBookingTime();
        LocalDateTime shiftEnd = calculateShiftEndTime(shiftStart, bookingDetail);

        List<Assignment> conflictingAssignments = assignmentRepository.findConflictingAssignments(employeeId, shiftStart, shiftEnd);
        if (!conflictingAssignments.isEmpty()) {
            throw new IllegalStateException("Nhân viên đã được phân công công việc khác trong khung giờ này");
        }

        List<EmployeeUnavailability> unavailabilities =
                employeeUnavailabilityRepository.findByEmployeeAndPeriod(employeeId, shiftStart, shiftEnd);
        boolean hasLeaveConflict = employeeUnavailabilityRepository.hasConflict(employeeId, shiftStart, shiftEnd);
        if (!unavailabilities.isEmpty() || hasLeaveConflict) {
            throw new IllegalStateException("Nhân viên đang có lịch nghỉ được phê duyệt trong khung giờ này");
        }

        if (bookingDetail.getAssignments().size() >= bookingDetail.getQuantity()) {
            throw new IllegalStateException("Chi tiết dịch vụ đã có đủ nhân viên");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        boolean alreadyAssigned = assignmentRepository.existsByBookingDetailIdAndEmployeeEmployeeId(detailId, employeeId);
        if (alreadyAssigned) {
            throw new IllegalStateException("Nhân viên đã nhận chi tiết dịch vụ này");
        }

        Assignment assignment = new Assignment();
        assignment.setBookingDetail(bookingDetail);
        assignment.setEmployee(employee);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignmentRepository.save(assignment);

        bookingDetail.getAssignments().add(assignment);

        boolean allAssigned = booking.getBookingDetails().stream()
                .allMatch(bd -> bd.getAssignments().size() >= bd.getQuantity());
        if (allAssigned && booking.getStatus() == BookingStatus.AWAITING_EMPLOYEE) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
        }

        return mapToAssignmentDetailResponse(assignment);
    }

    private LocalDateTime calculateShiftEndTime(LocalDateTime shiftStart, BookingDetail bookingDetail) {
        if (shiftStart == null) {
            throw new IllegalArgumentException("Booking không có thời gian bắt đầu hợp lệ");
        }

        if (bookingDetail.getService() == null || bookingDetail.getService().getEstimatedDurationHours() == null) {
            return shiftStart.plusHours(2);
        }

        var duration = bookingDetail.getService().getEstimatedDurationHours();
        long hours = duration.longValue();
        long minutes = duration.remainder(java.math.BigDecimal.ONE)
                .multiply(java.math.BigDecimal.valueOf(60))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValue();

        if (minutes >= 60) {
            hours += minutes / 60;
            minutes = minutes % 60;
        }

        return shiftStart.plusHours(hours).plusMinutes(minutes);
    }


    @Override
    @Transactional
    public boolean cancelAssignment(String assignmentId, AssignmentCancelRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công việc"));

        // Check if assignment can be cancelled
        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new IllegalStateException("Chỉ có thể hủy công việc đang ở trạng thái 'Đã nhận'");
        }

        BookingDetail bookingDetail = assignment.getBookingDetail();
        Booking booking = bookingDetail.getBooking();

        // Check if booking is not too close to start time (e.g., within 2 hours)
        LocalDateTime now = LocalDateTime.now();
        if (booking.getBookingTime().isBefore(now.plusHours(2))) {
            throw new IllegalStateException("Không thể hủy công việc trong vòng 2 giờ trước giờ bắt đầu");
        }

        try {
            // Update assignment status
            assignment.setStatus(AssignmentStatus.CANCELLED);
            assignment.setUpdatedAt(now);
            assignmentRepository.save(assignment);

            // Update booking status if all assignments are cancelled
            updateBookingStatusIfNeeded(booking.getBookingId());

            // Send crisis notification to customer
            sendCrisisNotification(booking, assignment, request.reason());

            log.info("Assignment {} cancelled by employee {}. Reason: {}",
                    assignmentId, assignment.getEmployee().getEmployeeId(), request.reason());

            return true;

        } catch (Exception e) {
            log.error("Failed to cancel assignment {}: {}", assignmentId, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi hủy công việc: " + e.getMessage());
        }
    }

    private void updateBookingStatusIfNeeded(String bookingId) {
        List<Assignment> bookingAssignments = assignmentRepository.findByBookingIdWithStatus(bookingId);

        boolean allCancelled = bookingAssignments.stream()
                .allMatch(a -> a.getStatus() == AssignmentStatus.CANCELLED);

        if (allCancelled) {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null && booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);
                log.info("Booking {} status updated to CANCELLED due to all assignments being cancelled", bookingId);
            }
        }
    }

    private void sendCrisisNotification(Booking booking, Assignment assignment, String reason) {
        try {
            // Send immediate notification to customer
            String message = String.format(
                    "THÔNG BÁO KHẨN: Lịch dọn dẹp %s của bạn vào %s đã bị hủy bởi nhân viên. " +
                    "Lý do: %s. Vui lòng liên hệ 1900-xxx để được hỗ trợ đặt lại dịch vụ.",
                    booking.getBookingCode(),
                    booking.getBookingTime(),
                    reason
            );

            //TODO: Implement notification service

//            notificationService.sendCrisisNotification(
//                    booking.getCustomer().getCustomerId(),
//                    "Lịch dịch vụ bị hủy khẩn cấp",
//                    message
//            );

            // Log for admin monitoring
            log.warn("CRISIS: Assignment {} cancelled by employee {}. Booking: {}, Customer: {}, Reason: {}",
                    assignment.getAssignmentId(),
                    assignment.getEmployee().getFullName(),
                    booking.getBookingCode(),
                    booking.getCustomer().getFullName(),
                    reason
            );

        } catch (Exception e) {
            log.error("Failed to send crisis notification for cancelled assignment {}: {}",
                    assignment.getAssignmentId(), e.getMessage(), e);
        }
    }

    private AssignmentDetailResponse mapToAssignmentDetailResponse(Assignment assignment) {
        BookingDetail bookingDetail = assignment.getBookingDetail();
        Booking booking = bookingDetail.getBooking();

        return new AssignmentDetailResponse(
                assignment.getAssignmentId(),
                booking.getBookingCode(),
                bookingDetail.getService().getName(),
                booking.getCustomer().getFullName(),
                booking.getCustomer().getAccount().getPhoneNumber(),
                booking.getAddress().getFullAddress(),
                booking.getBookingTime(),
                bookingDetail.getService().getEstimatedDurationHours(),
                bookingDetail.getPricePerUnit(),
                bookingDetail.getQuantity(),
                bookingDetail.getSubTotal(),
                assignment.getStatus(),
                assignment.getCreatedAt(),
                assignment.getCheckInTime(),
                assignment.getCheckOutTime(),
                booking.getNote()
        );
    }
}