package iuh.house_keeping_service_be.services.ReviewService.impl;

import iuh.house_keeping_service_be.dtos.Review.CriteriaRatingRequest;
import iuh.house_keeping_service_be.dtos.Review.PendingReviewResponse;
import iuh.house_keeping_service_be.dtos.Review.PendingReviewWebSocketEvent;
import iuh.house_keeping_service_be.dtos.Review.ReviewCreateRequest;
import iuh.house_keeping_service_be.dtos.Review.ReviewDetailResponse;
import iuh.house_keeping_service_be.dtos.Review.ReviewResponse;
import iuh.house_keeping_service_be.dtos.Review.ReviewSummaryResponse;
import iuh.house_keeping_service_be.dtos.Review.ReviewableEmployeeResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
            throw new ReviewPermissionException("Bạn không thể đánh giá đơn đặt của người khác");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ReviewBookingStateException("Chỉ có thể đánh giá khi dịch vụ đã hoàn thành");
        }

        if (!assignmentRepository.existsByBookingDetail_Booking_BookingIdAndEmployee_EmployeeId(
                booking.getBookingId(), request.employeeId())) {
            throw new ReviewAssignmentException("Nhân viên không được phân công cho đơn đặt này");
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
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> EmployeeNotFoundException.withId(employeeId));
        
        long totalReviews = reviewRepository.countByEmployee_EmployeeId(employeeId);
        Double averageRating = reviewDetailRepository.findAverageRatingByEmployeeId(employeeId);
        Rating ratingTier = totalReviews > 0 ? Rating.fromAverage(averageRating) : null;
        double average = averageRating != null ?
                BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0;
        
        // Calculate rating distribution (1-5 stars)
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        List<Object[]> distribution = reviewDetailRepository.findRatingDistributionByEmployeeId(employeeId);
        for (Object[] row : distribution) {
            Integer star = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            ratingDistribution.put(star, count);
        }
        
        return new ReviewSummaryResponse(
                employeeId,
                employee.getFullName(),
                employee.getAvatar(),
                totalReviews,
                average,
                ratingTier,
                ratingDistribution
        );
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<PendingReviewResponse> getPendingReviewsForCustomer(String authorizationHeader, Pageable pageable) {
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

        List<PendingReviewResponse> allPending = new ArrayList<>(pendingMap.values());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allPending.size());
        
        List<PendingReviewResponse> pageContent = start >= allPending.size() 
                ? List.of() 
                : allPending.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, allPending.size());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<ReviewableEmployeeResponse> getReviewableEmployees(String authorizationHeader, String bookingId, Pageable pageable) {
        String username = authorizationService.getCurrentUserId(authorizationHeader);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseThrow(() -> CustomerNotFoundException.withCustomMessage(
                        "Không tìm thấy khách hàng cho tài khoản: " + username
                ));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingNotFoundException.withId(bookingId));

        // Validate customer owns this booking
        if (!booking.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new ReviewPermissionException("Bạn không có quyền xem thông tin đánh giá của đơn đặt này");
        }

        // Check booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ReviewBookingStateException("Chỉ có thể xem nhân viên cần đánh giá khi dịch vụ đã hoàn thành");
        }

        // Get all assignments for this booking
        List<Assignment> assignments = assignmentRepository
                .findByBookingDetail_Booking_BookingId(bookingId);

        Map<String, ReviewableEmployeeResponse> employeeMap = new LinkedHashMap<>();

        for (Assignment assignment : assignments) {
            Employee employee = assignment.getEmployee();
            if (employee == null || employee.getEmployeeId() == null) {
                continue;
            }

            // Check if already reviewed
            boolean alreadyReviewed = reviewRepository.existsByBooking_BookingIdAndEmployee_EmployeeId(
                    bookingId, employee.getEmployeeId());
            if (alreadyReviewed) {
                continue;
            }

            // Skip duplicates
            if (employeeMap.containsKey(employee.getEmployeeId())) {
                continue;
            }

            String serviceName = null;
            if (assignment.getBookingDetail() != null && assignment.getBookingDetail().getService() != null) {
                serviceName = assignment.getBookingDetail().getService().getName();
            }

            employeeMap.put(employee.getEmployeeId(), ReviewableEmployeeResponse.builder()
                    .employeeId(employee.getEmployeeId())
                    .employeeName(employee.getFullName())
                    .employeeAvatar(employee.getAvatar())
                    .serviceName(serviceName)
                    .build());
        }

        List<ReviewableEmployeeResponse> allEmployees = new ArrayList<>(employeeMap.values());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allEmployees.size());
        
        List<ReviewableEmployeeResponse> pageContent = start >= allEmployees.size() 
                ? List.of() 
                : allEmployees.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, allEmployees.size());
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

        // Calculate average rating from details
        double averageRating = review.getDetails().stream()
                .mapToDouble(detail -> detail.getRating().doubleValue())
                .average()
                .orElse(0.0);
        averageRating = BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return new ReviewResponse(
                review.getReviewId(),
                review.getBooking().getBookingId(),
                review.getBooking().getBookingCode(),
                review.getCustomer().getCustomerId(),
                review.getCustomer().getFullName(),
                review.getEmployee().getEmployeeId(),
                review.getEmployee().getFullName(),
                review.getEmployee().getAvatar(),
                review.getComment(),
                averageRating,
                review.getCreatedAt(),
                detailResponses
        );
    }
}
