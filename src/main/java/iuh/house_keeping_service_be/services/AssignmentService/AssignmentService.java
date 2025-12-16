package iuh.house_keeping_service_be.services.AssignmentService;

import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentActionRequest;
import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCancelRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentStatisticsByStatusResponse;
import iuh.house_keeping_service_be.dtos.Assignment.response.BookingSummary;
import iuh.house_keeping_service_be.dtos.Employee.response.EmployeeBookingStatisticsByStatusResponse;
import iuh.house_keeping_service_be.dtos.common.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentService {
    PageResponse<AssignmentDetailResponse> getEmployeeAssignments(String employeeId, String status, int page, int size);
    boolean cancelAssignment(String assignmentId, AssignmentCancelRequest request);
    AssignmentDetailResponse acceptAssignment(String assignmentId, String employeeId);

    List<BookingSummary> getAvailableBookings(String employeeId, int page, int size);
    AssignmentDetailResponse acceptBookingDetail(String detailId, String employeeId);

    AssignmentDetailResponse checkIn(String assignmentId, String employeeId, List<MultipartFile> images, String imageDescription, Double latitude, Double longitude);
    AssignmentDetailResponse checkOut(String assignmentId, String employeeId, List<MultipartFile> images, String imageDescription, Double latitude, Double longitude);
    
    // Get assignment statistics by status and time unit for employee
    AssignmentStatisticsByStatusResponse getAssignmentStatisticsByStatus(String employeeId, String timeUnit, LocalDateTime startDate, LocalDateTime endDate);
    
    // Get booking statistics by status and time unit for employee
    EmployeeBookingStatisticsByStatusResponse getEmployeeBookingStatisticsByStatus(String employeeId, String timeUnit, LocalDateTime startDate, LocalDateTime endDate);
}