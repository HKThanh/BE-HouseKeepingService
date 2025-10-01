package iuh.house_keeping_service_be.services.ReviewService;

import iuh.house_keeping_service_be.dtos.Review.ReviewCreateRequest;
import iuh.house_keeping_service_be.dtos.Review.ReviewResponse;
import iuh.house_keeping_service_be.dtos.Review.ReviewSummaryResponse;
import iuh.house_keeping_service_be.models.ReviewCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(String authorizationHeader, ReviewCreateRequest request);

    Page<ReviewResponse> getReviewsForEmployee(String employeeId, Pageable pageable);

    List<ReviewCriteria> getAllCriteria();

    ReviewSummaryResponse getEmployeeSummary(String employeeId);
}