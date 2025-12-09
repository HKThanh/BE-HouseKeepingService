package iuh.house_keeping_service_be.dtos.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewableEmployeeResponse {
    private String employeeId;
    private String employeeName;
    private String employeeAvatar;
    private String serviceName;
}
