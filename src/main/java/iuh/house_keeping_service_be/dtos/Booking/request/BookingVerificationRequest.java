package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.constraints.NotNull;

public record BookingVerificationRequest(
    @NotNull(message = "Quyết định xác minh không được để trống")
    Boolean approve,
    
    String rejectionReason,
    
    String adminComment
) {
}
