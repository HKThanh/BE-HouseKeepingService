package iuh.house_keeping_service_be.dtos.Review;

import iuh.house_keeping_service_be.enums.Rating;

import java.util.Map;

public record ReviewSummaryResponse(
        String employeeId,
        String employeeName,
        String employeeAvatar,
        long totalReviews,
        double averageRating,
        Rating ratingTier,
        Map<Integer, Long> ratingDistribution
) {
}