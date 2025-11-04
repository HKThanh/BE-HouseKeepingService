package iuh.house_keeping_service_be.dtos.Assignment.request;

import jakarta.validation.constraints.NotBlank;

public record AssignmentCheckInRequest(
        @NotBlank(message = "Mã nhân viên là bắt buộc")
        String employeeId,
        
        String imageDescription  // Optional description for the check-in image
) {
}
