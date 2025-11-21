package iuh.house_keeping_service_be.services.ReviewService.impl;

import iuh.house_keeping_service_be.dtos.Review.CriteriaRatingRequest;
import iuh.house_keeping_service_be.dtos.Review.PendingReviewResponse;
import iuh.house_keeping_service_be.dtos.Review.PendingReviewWebSocketEvent;
import iuh.house_keeping_service_be.dtos.Review.ReviewCreateRequest;
import iuh.house_keeping_service_be.dtos.Review.ReviewDetailResponse;
import iuh.house_keeping_service_be.dtos.Review.ReviewResponse;
import iuh.house_keeping_service_be.dtos.Review.ReviewSummaryResponse;
import iuh.house_keeping_service_be.enums.BookingStatus;
import iuh.house_keeping_service_be.enums.Rating;
import iuh.house_keeping_service_be.exceptions.*;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import iuh.house_keeping_service_be.services.AuthorizationService.AuthorizationService;
import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import iuh.house_keeping_service_be.services.ReviewService.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final PermissionService permissionService;
    private final AuthorizationService authorizationService;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final AssignmentRepository assignmentRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewDetailRepository reviewDetailRepository;
    private final ReviewCriteriaRepository reviewCriteriaRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public ReviewResponse createReview(String authorizationHeader, ReviewCreateRequest request) {
        String username = authorizationService.getCurrentUserId(authorizationHeader);
        if (!permissionService.hasPermission(username, "review.create")) {
            throw new ReviewPermissionException("Bạn không có quyền tạo đánh giá");
        }

        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> CustomerNotFoundException.withCustomMessage(
                        "Không tìm thấy khách hàng cho tài khoản: " + username
                ));

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> BookingNotFoundException.withId(request.bookingId()));

        if (!booking.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new ReviewPermissionException("Bạn không thể đánh giá đặt chỗ của người khác");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ReviewBookingStateException("Chỉ có thể đánh giá khi dịch vụ đã hoàn thành");
        }

        if (!assignmentRepository.existsByBookingDetail_Booking_BookingIdAndEmployee_EmployeeId(
                booking.getBookingId(), request.employeeId())) {
            throw new ReviewAssignmentException("Nhân viên không được phân công cho đơn đặt chỗ này");
        }

        if (reviewRepository.existsByBooking_BookingIdAndEmployee_EmployeeId(
                booking.getBookingId(), request.employeeId())) {
            throw new ReviewAlreadyExistsException("Bạn đã đánh giá nhân viên cho đơn này rồi");
        }

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> EmployeeNotFoundException.withId(request.employeeId()));

        Review review = new Review();
        review.setBooking(booking);
        review.setCustomer(customer);
        review.setEmployee(employee);
        review.setComment(request.comment());

        Map<Integer, ReviewCriteria> criteriaById = reviewCriteriaRepository
                .findAllById(request.criteriaRatings().stream()
                        .map(CriteriaRatingRequest::criteriaId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(ReviewCriteria::getCriteriaId, Function.identity()));

        for (CriteriaRatingRequest ratingRequest : request.criteriaRatings()) {
            ReviewCriteria criteria = criteriaById.get(ratingRequest.criteriaId());
            if (criteria == null) {
                throw new ReviewCriteriaNotFoundException(
                        "Không tìm thấy tiêu chí đánh giá với ID: " + ratingRequest.criteriaId()
                );
            }

            ReviewDetail detail = new ReviewDetail();
            detail.setCriteria(criteria);
            detail.setRating(BigDecimal.valueOf(ratingRequest.rating())
                    .setScale(1, RoundingMode.HALF_UP));
            detail.getId().setCriteriaId(criteria.getCriteriaId());
            review.addDetail(detail);
        }

        Review savedReview = reviewRepository.save(review);

        Double averageRating = reviewDetailRepository.findAverageRatingByEmployeeId(employee.getEmployeeId());
        Rating ratingTier = Rating.fromAverage(averageRating);
        employee.setRating(ratingTier);
        employeeRepository.save(employee);

        notifyEmployeeReviewReceived(savedReview, employee, request.criteriaRatings());
        sendPendingReviewEventToCustomer(
                booking.getCustomer(),
                PendingReviewWebSocketEvent.builder()
                        .action(PendingReviewWebSocketEvent.Action.REMOVE)
                        .payload(PendingReviewResponse.builder()
                                .bookingId(booking.getBookingId())
                                .bookingCode(booking.getBookingCode())
                                .bookingTime(booking.getBookingTime())
                                .employeeId(employee.getEmployeeId())
                                .employeeName(employee.getFullName())
                                .employeeAvatar(employee.getAvatar())
                                .build())
                        .bookingId(booking.getBookingId())
                        .employeeId(employee.getEmployeeId())
                        .build()
        );
        return toReviewResponse(savedReview);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ReviewResponse> getReviewsForEmployee(String employeeId, Pageable pageable) {
        return reviewRepository.findByEmployee_EmployeeId(employeeId, pageable)
                .map(this::toReviewResponse);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ReviewCriteria> getAllCriteria() {
        return reviewCriteriaRepository.findAll();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public ReviewSummaryResponse getEmployeeSummary(String employeeId) {
        long totalReviews = reviewRepository.countByEmployee_EmployeeId(employeeId);
        Double averageRating = reviewDetailRepository.findAverageRatingByEmployeeId(employeeId);
        Rating ratingTier = totalReviews > 0 ? Rating.fromAverage(averageRating) : null;
        double average = averageRating != null ?
                BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0;
        return new ReviewSummaryResponse(employeeId, totalReviews, average, ratingTier);
    }

    @Override
    public List<PendingReviewResponse> getPendingReviewsForCustomer(String authorizationHeader) {
        String username = authorizationService.getCurrentUserId(authorizationHeader);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> CustomerNotFoundException.withCustomMessage(
                        "Không tìm thấy khách hàng cho tài khoản: " + username
                ));

        List<Assignment> completedAssignments = assignmentRepository
                .findCompletedAssignmentsByCustomer(customer.getCustomerId());

        Map<String, PendingReviewResponse> pendingMap = new LinkedHashMap<>();

        for (Assignment assignment : completedAssignments) {
            Booking booking = assignment.getBookingDetail() != null
                    ? assignment.getBookingDetail().getBooking()
                    : null;
            Employee employee = assignment.getEmployee();

            if (booking == null || employee == null || employee.getEmployeeId() == null) {
                continue;
            }

            boolean alreadyReviewed = reviewRepository.existsByBooking_BookingIdAndEmployee_EmployeeId(
                    booking.getBookingId(), employee.getEmployeeId());
            if (alreadyReviewed) {
                continue;
            }

            String key = booking.getBookingId() + "|" + employee.getEmployeeId();
            if (pendingMap.containsKey(key)) {
                continue;
            }

            String serviceName = null;
            if (assignment.getBookingDetail() != null && assignment.getBookingDetail().getService() != null) {
                serviceName = assignment.getBookingDetail().getService().getName();
            }

            pendingMap.put(key, PendingReviewResponse.builder()
                    .bookingId(booking.getBookingId())
                    .bookingCode(booking.getBookingCode())
                    .bookingTime(booking.getBookingTime())
                    .assignmentId(assignment.getAssignmentId())
                    .serviceName(serviceName)
                    .employeeId(employee.getEmployeeId())
                    .employeeName(employee.getFullName())
                    .employeeAvatar(employee.getAvatar())
                    .build());
        }

        return pendingMap.values().stream().toList();
    }

    private void notifyEmployeeReviewReceived(Review review,
                                              Employee employee,
                                              List<CriteriaRatingRequest> criteriaRatings) {
        if (review == null || employee == null || employee.getAccount() == null ||
                criteriaRatings == null || criteriaRatings.isEmpty()) {
            return;
        }

        double average = criteriaRatings.stream()
                .mapToDouble(CriteriaRatingRequest::rating)
                .average()
                .orElse(0D);

        int roundedRating = (int) Math.max(1, Math.min(5, Math.round(average)));
        notificationService.sendReviewReceivedNotification(
                employee.getAccount().getAccountId(),
                review.getReviewId() != null ? String.valueOf(review.getReviewId()) : null,
                roundedRating
        );
    }

    private void sendPendingReviewEventToCustomer(Customer customer, PendingReviewWebSocketEvent event) {
        if (customer == null || customer.getAccount() == null || event == null) {
            return;
        }
        String accountId = customer.getAccount().getAccountId();
        messagingTemplate.convertAndSend("/topic/reviews/pending/" + accountId, event);
    }

    private ReviewResponse toReviewResponse(Review review) {
        List<ReviewDetailResponse> detailResponses = review.getDetails().stream()
                .map(detail -> new ReviewDetailResponse(
                        detail.getCriteria().getCriteriaId(),
                        detail.getCriteria().getCriteriaName(),
                        detail.getRating().doubleValue()
                ))
                .toList();

        return new ReviewResponse(
                review.getReviewId(),
                review.getBooking().getBookingId(),
                review.getCustomer().getCustomerId(),
                review.getEmployee().getEmployeeId(),
                review.getComment(),
                review.getCreatedAt(),
                detailResponses
        );
    }
}
