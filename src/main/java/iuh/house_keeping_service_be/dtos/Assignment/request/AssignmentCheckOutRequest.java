package iuh.house_keeping_service_be.dtos.Assignment.request;

import jakarta.validation.constraints.NotBlank;

public record AssignmentCheckOutRequest(
        @NotBlank(message = "Mã nhân viên là bắt buộc")
        String employeeId,
        
        String imageDescription,  // Optional description for the check-out image
        
        Double latitude,  // Optional latitude coordinate for check-out location
        
        Double longitude  // Optional longitude coordinate for check-out location
) {
}
