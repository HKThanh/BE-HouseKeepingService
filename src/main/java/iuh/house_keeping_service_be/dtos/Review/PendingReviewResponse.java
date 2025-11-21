package iuh.house_keeping_service_be.dtos.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingReviewResponse {
    private String bookingId;
    private String bookingCode;
    private LocalDateTime bookingTime;
    private String assignmentId;
    private String serviceName;
    private String employeeId;
    private String employeeName;
    private String employeeAvatar;
}
