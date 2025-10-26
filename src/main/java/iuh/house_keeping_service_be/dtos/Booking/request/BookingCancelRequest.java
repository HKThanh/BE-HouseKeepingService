package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.constraints.Size;

public record BookingCancelRequest(
    @Size(max = 500, message = "Lý do hủy không được quá 500 ký tự")
    String reason
) {
}
