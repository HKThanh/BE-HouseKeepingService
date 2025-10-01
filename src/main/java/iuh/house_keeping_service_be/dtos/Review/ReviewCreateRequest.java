package iuh.house_keeping_service_be.dtos.Review;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewCreateRequest(
        @NotBlank(message = "Booking ID is required")
        String bookingId,

        @NotBlank(message = "Employee ID is required")
        String employeeId,

        @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
        String comment,

        @Valid
        @NotEmpty(message = "Ratings per criteria are required")
        List<CriteriaRatingRequest> criteriaRatings
) {
}