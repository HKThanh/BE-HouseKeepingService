package iuh.house_keeping_service_be.dtos.Assignment.response;

public record AssignmentActionResponse(
        boolean success,
        String message,
        AssignmentDetailResponse assignment
) {
}