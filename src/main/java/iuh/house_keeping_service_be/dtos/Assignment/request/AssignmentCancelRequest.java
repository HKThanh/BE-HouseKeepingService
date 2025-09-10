package iuh.house_keeping_service_be.dtos.Assignment.request;

import jakarta.validation.constraints.NotBlank;

public record AssignmentCancelRequest(
        @NotBlank(message = "Lý do hủy là bắt buộc")
        String reason,

        String note
) {}