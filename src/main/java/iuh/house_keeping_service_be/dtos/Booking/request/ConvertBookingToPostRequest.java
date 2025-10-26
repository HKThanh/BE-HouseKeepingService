package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.constraints.NotBlank;

public record ConvertBookingToPostRequest(
    @NotBlank(message = "Tiêu đề không được để trống")
    String title,
    
    @NotBlank(message = "URL hình ảnh không được để trống")
    String imageUrl
) {
}
