package iuh.house_keeping_service_be.services.AssignmentService;

import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentActionRequest;
import iuh.house_keeping_service_be.dtos.Assignment.request.AssignmentCancelRequest;
import iuh.house_keeping_service_be.dtos.Assignment.response.AssignmentDetailResponse;
import iuh.house_keeping_service_be.dtos.Assignment.response.BookingSummary;

import java.util.List;

public interface AssignmentService {
    List<AssignmentDetailResponse> getEmployeeAssignments(String employeeId, String status, int page, int size);
    boolean cancelAssignment(String assignmentId, AssignmentCancelRequest request);
//    boolean acceptBooking(String assignmentId);

    List<BookingSummary> getAvailableBookings(String employeeId, int page, int size);
    AssignmentDetailResponse acceptBookingDetail(String detailId, String employeeId);

    AssignmentDetailResponse checkIn(String assignmentId, AssignmentActionRequest request);
    AssignmentDetailResponse checkOut(String assignmentId, AssignmentActionRequest request);
}