package iuh.house_keeping_service_be.dtos.Assignment;

import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentProgressWebSocketEvent {
    private String action; // CHECK_IN, CHECK_OUT
    private String bookingId;
    private String bookingCode;
    private String assignmentId;
    private String employeeId;
    private String employeeName;
    private AssignmentStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BookingStatus bookingStatusAfterUpdate;
    private LocalDateTime at;
}
