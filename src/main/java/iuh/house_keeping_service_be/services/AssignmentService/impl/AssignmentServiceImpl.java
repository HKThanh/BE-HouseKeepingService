package iuh.house_keeping_service_be.services.AssignmentService.impl;

import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCancelRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.models.Assignment;
import iuh.house_keeping_service_be.models.BookingDetail;
import iuh.house_keeping_service_be.models.Booking;
import iuh.house_keeping_service_be.repositories.AssignmentRepository;
import iuh.house_keeping_service_be.repositories.BookingRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final BookingRepository bookingRepository;
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