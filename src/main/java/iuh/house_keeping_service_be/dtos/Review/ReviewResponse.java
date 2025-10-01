package iuh.house_keeping_service_be.dtos.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Integer reviewId,
        String bookingId,
        String customerId,
        String employeeId,
        String comment,
        LocalDateTime createdAt,
        List<ReviewDetailResponse> details
) {
}