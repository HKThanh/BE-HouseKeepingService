package iuh.house_keeping_service_be.dtos.Review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CriteriaRatingRequest(
        @NotNull(message = "Criteria ID is required")
        Integer criteriaId,

        @NotNull(message = "Rating is required")
        @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
        @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
        Double rating
) {
}