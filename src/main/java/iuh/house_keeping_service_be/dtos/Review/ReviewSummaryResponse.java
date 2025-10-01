package iuh.house_keeping_service_be.dtos.Review;

import iuh.house_keeping_service_be.enums.Rating;

public record ReviewSummaryResponse(
        String employeeId,
        long totalReviews,
        double averageRating,
        Rating ratingTier
) {
}