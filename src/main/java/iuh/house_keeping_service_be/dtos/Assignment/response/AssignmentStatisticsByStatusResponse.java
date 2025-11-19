package iuh.house_keeping_service_be.dtos.Assignment.response;

import iuh.house_keeping_service_be.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentStatisticsByStatusResponse {
    private String timeUnit; // DAY, WEEK, MONTH, YEAR
    private String startDate;
    private String endDate;
    private long totalAssignments;
    private Map<AssignmentStatus, Long> countByStatus;
}
