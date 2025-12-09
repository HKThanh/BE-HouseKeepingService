package iuh.house_keeping_service_be.dtos.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Integer reviewId,
        String bookingId,
        String bookingCode,
        String customerId,
        String customerName,
        String employeeId,
        String employeeName,
        String employeeAvatar,
        String comment,
        Double averageRating,
        LocalDateTime createdAt,
        List<ReviewDetailResponse> details
) {
}