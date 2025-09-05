package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssignmentRequest(
        @NotBlank(message = "Employee ID is required")
        String employeeId,

        @NotNull(message = "Service ID is required")
        Integer serviceId
) {}