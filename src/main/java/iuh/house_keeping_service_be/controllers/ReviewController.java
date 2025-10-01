package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Review.ReviewCreateRequest;
import iuh.house_keeping_service_be.dtos.Review.ReviewResponse;
import iuh.house_keeping_service_be.dtos.Review.ReviewSummaryResponse;
import iuh.house_keeping_service_be.models.ReviewCriteria;
import iuh.house_keeping_service_be.services.ReviewService.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ReviewResponse> createReview(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewResponse response = reviewService.createReview(authorizationHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reviews/criteria")
    public ResponseEntity<List<ReviewCriteria>> getReviewCriteria() {
        return ResponseEntity.ok(reviewService.getAllCriteria());
    }

    @GetMapping("/employees/{employeeId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviewsForEmployee(
            @PathVariable String employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewResponse> reviews = reviewService.getReviewsForEmployee(employeeId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/employees/{employeeId}/reviews/summary")
    public ResponseEntity<ReviewSummaryResponse> getEmployeeReviewSummary(@PathVariable String employeeId) {
        return ResponseEntity.ok(reviewService.getEmployeeSummary(employeeId));
    }
}