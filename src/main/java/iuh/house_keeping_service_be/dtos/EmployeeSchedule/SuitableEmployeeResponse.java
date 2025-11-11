package iuh.house_keeping_service_be.dtos.EmployeeSchedule;

import java.util.List;

public record SuitableEmployeeResponse(
        String employeeId,
        String fullName,
        String avatar,
        List<String> skills,
        String rating,
        String status,
        String[] workingWards,
        String workingCity,
        Integer completedJobs,
        Boolean hasWorkedWithCustomer,  // true nếu nhân viên đã từng phục vụ customer này
        RecommendationMetadata recommendation
) {
}
