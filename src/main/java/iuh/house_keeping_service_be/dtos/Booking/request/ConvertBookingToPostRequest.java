package iuh.house_keeping_service_be.dtos.Booking.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ConvertBookingToPostRequest(
    @NotBlank(message = "Tiêu đề không được để trống")
    String title,
    
    @NotEmpty(message = "Danh sách URL hình ảnh không được để trống")
    List<String> imageUrls
) {
}
