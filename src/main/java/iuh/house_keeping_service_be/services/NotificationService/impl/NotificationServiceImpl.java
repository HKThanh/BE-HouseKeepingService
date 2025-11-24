package iuh.house_keeping_service_be.services.NotificationService.impl;

import iuh.house_keeping_service_be.dtos.Notification.NotificationRequest;
import iuh.house_keeping_service_be.dtos.Notification.NotificationResponse;
import iuh.house_keeping_service_be.models.Notification;
import iuh.house_keeping_service_be.repositories.NotificationRepository;
import iuh.house_keeping_service_be.services.EmailService.EmailRecipientResolver;
import iuh.house_keeping_service_be.services.EmailService.EmailService;
import iuh.house_keeping_service_be.services.NotificationService.NotificationService;
import iuh.house_keeping_service_be.services.WebSocketNotificationService.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final EmailRecipientResolver emailRecipientResolver;
    private final WebSocketNotificationService webSocketNotificationService;
    @Lazy
    @Autowired
    private NotificationServiceImpl self;
    
    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for account: {}", request.accountId());
        
        Notification notification = Notification.builder()
                .accountId(request.accountId())
                .targetRole(request.targetRole())
                .type(request.type())
                .title(request.title())
                .message(request.message())
                .relatedId(request.relatedId())
                .relatedType(request.relatedType())
                .priority(request.priority() != null ? request.priority() : Notification.NotificationPriority.NORMAL)
                .actionUrl(request.actionUrl())
                .isRead(false)
                .build();
        
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created successfully: {} for role: {}", 
                saved.getNotificationId(), saved.getTargetRole());
        
        // Dispatch channels asynchronously to avoid slowing down request thread
        self.dispatchNotificationChannelsAsync(saved);
        
        return NotificationResponse.fromEntity(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByAccountId(String accountId, Pageable pageable) {
        log.info("Getting notifications for account: {}", accountId);
        
        Page<Notification> notifications = notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable);
        
        return notifications.map(NotificationResponse::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(String accountId, Pageable pageable) {
        log.info("Getting unread notifications for account: {}", accountId);
        
        Page<Notification> notifications = notificationRepository.findByAccountIdAndIsReadFalseOrderByCreatedAtDesc(accountId, pageable);
        
        return notifications.map(NotificationResponse::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(String notificationId) {
        log.info("Getting notification by ID: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo với ID: " + notificationId));
        
        return NotificationResponse.fromEntity(notification);
    }
    
    @Override
    @Transactional
    public NotificationResponse markAsRead(String notificationId) {
        log.info("Marking notification as read: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo với ID: " + notificationId));
        
        if (!notification.getIsRead()) {
            notification.markAsRead();
            notification = notificationRepository.save(notification);
            log.info("Notification marked as read: {}", notificationId);
        }
        
        return NotificationResponse.fromEntity(notification);
    }
    
    @Override
    @Transactional
    public int markAllAsRead(String accountId) {
        log.info("Marking all notifications as read for account: {}", accountId);
        
        int updated = notificationRepository.markAllAsReadByAccountId(accountId, LocalDateTime.now());
        
        log.info("Marked {} notifications as read for account: {}", updated, accountId);
        return updated;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String accountId) {
        return notificationRepository.countUnreadByAccountId(accountId);
    }
    
    @Override
    @Transactional
    public void deleteNotification(String notificationId) {
        log.info("Deleting notification: {}", notificationId);
        
        if (!notificationRepository.existsById(notificationId)) {
            throw new RuntimeException("Không tìm thấy thông báo với ID: " + notificationId);
        }
        
        notificationRepository.deleteById(notificationId);
        log.info("Notification deleted: {}", notificationId);
    }
    
    @Override
    @Transactional
    public int cleanupOldNotifications(int daysOld) {
        log.info("Cleaning up notifications older than {} days", daysOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deleted = notificationRepository.deleteOldReadNotifications(cutoffDate);
        
        log.info("Deleted {} old read notifications", deleted);
        return deleted;
    }

    @Async
    protected void dispatchNotificationChannelsAsync(Notification notification) {
        try {
            dispatchEmailNotification(notification);
        } catch (Exception e) {
            log.error("Failed to send email notification asynchronously for {}: {}", notification.getNotificationId(), e.getMessage(), e);
        }

        try {
            webSocketNotificationService.sendNotificationToUser(
                    notification.getAccountId(),
                    notification.getTargetRole(),
                    notification
            );
        } catch (Exception e) {
            log.error("Failed to send websocket notification asynchronously for {}: {}", notification.getNotificationId(), e.getMessage(), e);
        }
    }
    
    // ========== Specific Notification Methods ==========
    
    @Override
    @Transactional
    public void sendBookingCreatedNotification(String accountId, String bookingId, String bookingCode) {
        NotificationRequest request = new NotificationRequest(
                accountId,
                "CUSTOMER", // Target role
                Notification.NotificationType.BOOKING_CREATED,
                "Đặt lịch thành công",
                String.format("Booking %s của bạn đã được tạo thành công và đang chờ xác minh.", bookingCode),
                bookingId,
                Notification.RelatedEntityType.BOOKING,
                Notification.NotificationPriority.NORMAL,
                "/bookings/" + bookingId
        );
        
        createNotification(request);
    }
    
    @Override
    @Transactional
    public void sendBookingConfirmedNotification(String accountId, String bookingId, String bookingCode) {
        NotificationRequest request = new NotificationRequest(
                accountId,
                "CUSTOMER", // Target role
                Notification.NotificationType.BOOKING_CONFIRMED,
                "Booking đã được xác nhận",
                String.format("Booking %s của bạn đã được xác nhận. Nhân viên sẽ đến đúng giờ đã hẹn.", bookingCode),
                bookingId,
                Notification.RelatedEntityType.BOOKING,
                Notification.NotificationPriority.HIGH,
                "/bookings/" + bookingId
        );
        
        createNotification(request);
    }
    
    @Override
    @Transactional
    public void sendBookingCancelledNotification(String accountId, String bookingId, String bookingCode, String reason) {
        NotificationRequest request = new NotificationRequest(
                accountId,
                "CUSTOMER", // Target role
                Notification.NotificationType.BOOKING_CANCELLED,
                "Booking đã bị hủy",
                String.format("Booking %s đã bị hủy. Lý do: %s", bookingCode, reason),
                bookingId,
                Notification.RelatedEntityType.BOOKING,
                Notification.NotificationPriority.HIGH,
                "/bookings/" + bookingId
        );
        
        createNotification(request);
    }
    
    @Override
    @Transactional
    public void sendBookingVerifiedNotification(String accountId, String bookingId, String bookingCode, boolean approved) {
        String title = approved ? "Bài post được chấp nhận" : "Bài post bị từ chối";
        String message = approved 
                ? String.format("Bài post %s của bạn đã được Admin chấp nhận. Nhân viên có thể nhận việc.", bookingCode)
                : String.format("Bài post %s của bạn đã bị Admin từ chối. Vui lòng kiểm tra lý do.", bookingCode);
        
        NotificationRequest request = new NotificationRequest(
                accountId,
                "CUSTOMER", // Target role
                approved ? Notification.NotificationType.BOOKING_VERIFIED : Notification.NotificationType.BOOKING_REJECTED,
                title,
                message,
                bookingId,
                Notification.RelatedEntityType.BOOKING,
                Notification.NotificationPriority.HIGH,
                "/bookings/" + bookingId
        );
        
        createNotification(request);
    }
    
    @Override
    @Transactional
    public void sendAssignmentCreatedNotification(String accountId, String assignmentId, String bookingCode) {
        NotificationRequest request = new NotificationRequest(
                accountId,
                "EMPLOYEE", // Target role
                Notification.NotificationType.ASSIGNMENT_CREATED,
                "Bạn có công việc mới",
                String.format("Bạn đã được phân công làm việc cho booking %s. Vui lòng xem chi tiết.", bookingCode),
                assignmentId,
                Notification.RelatedEntityType.ASSIGNMENT,
                Notification.NotificationPriority.HIGH,
                "/assignments/" + assignmentId
        );
        
        createNotification(request);
    }
    
    @Override
    @Transactional
    public void sendAssignmentCancelledNotification(String accountId, String bookingId, String bookingCode, String reason) {
        NotificationRequest request = new NotificationRequest(
                accountId,
                "CUSTOMER", // Target role - Customer cần biết nhân viên hủy
                Notification.NotificationType.ASSIGNMENT_CRISIS,
                "KHẨN CẤP: Nhân viên hủy công việc",
                String.format("Nhân viên đã hủy công việc cho booking %s. Lý do: %s. Vui lòng liên hệ ngay để được hỗ trợ.", 
                        bookingCode, reason),
                bookingId,
                Notification.RelatedEntityType.BOOKING,
                Notification.NotificationPriority.URGENT,
                "/bookings/" + bookingId
        );
        
        createNotification(request);
    }

    @Override
    @Transactional
    public void sendAssignmentCancelledNotificationForEmployee(
            String accountId,
            String assignmentId,
            String bookingCode,
            String reason
    ) {
        String normalizedReason = (reason != null && !reason.trim().isEmpty())
                ? reason.trim()
                : "Khách hàng không cung cấp lý do";

        String bookingIdentifier = (bookingCode != null && !bookingCode.isBlank())
                ? bookingCode
                : "của bạn";

        NotificationRequest request = new NotificationRequest(
                accountId,
                "EMPLOYEE", // Notify employee assigned to this booking
                Notification.NotificationType.ASSIGNMENT_CANCELLED,
                "Công việc đã bị hủy",
                String.format("Booking %s đã bị khách hàng hủy. Lý do: %s", bookingIdentifier, normalizedReason),
                assignmentId,
                Notification.RelatedEntityType.ASSIGNMENT,
                Notification.NotificationPriority.HIGH,
                "/assignments/" + assignmentId
        );

        createNotification(request);
    }

    @Override
    @Transactional
    public void sendPaymentSuccessNotification(String accountId, String paymentId, double amount) {
        NotificationRequest request = new NotificationRequest(
                accountId,
                "CUSTOMER", // Target role
                Notification.NotificationType.PAYMENT_SUCCESS,
                "Thanh toán thành công",
                String.format("Thanh toán của bạn đã được xử lý thành công. Số tiền: %,.0f VND", amount),
                paymentId,
                Notification.RelatedEntityType.PAYMENT,
                Notification.NotificationPriority.NORMAL,
                "/payments/" + paymentId
        );
        
        createNotification(request);
    }
    
    @Override
    @Transactional
    public void sendReviewReceivedNotification(String accountId, String reviewId, int rating) {
        String stars = "⭐".repeat(rating);
        NotificationRequest request = new NotificationRequest(
                accountId,
                "EMPLOYEE", // Target role - Employee receives reviews
                Notification.NotificationType.REVIEW_RECEIVED,
                "Bạn nhận được đánh giá mới",
                String.format("Bạn đã nhận được đánh giá %s. Cảm ơn bạn đã sử dụng dịch vụ!", stars),
                reviewId,
                Notification.RelatedEntityType.REVIEW,
                Notification.NotificationPriority.NORMAL,
                "/reviews/" + reviewId
        );
        
        createNotification(request);
    }

    @Override
    @Transactional
    public void sendBookingCompletedNotification(String accountId, String bookingId, String bookingCode) {
        NotificationRequest request = new NotificationRequest(
                accountId,
                "CUSTOMER",
                Notification.NotificationType.BOOKING_COMPLETED,
                "Công việc đã hoàn tất",
                String.format("Nhân viên đã hoàn tất công việc cho booking %s. Vui lòng kiểm tra và tiến hành thanh toán.", bookingCode),
                bookingId,
                Notification.RelatedEntityType.BOOKING,
                Notification.NotificationPriority.HIGH,
                "/bookings/" + bookingId
        );

        createNotification(request);
    }

    @Override
    @Transactional
    public void sendReviewRequestNotification(String accountId, String bookingId, String bookingCode, String employeeNames) {
        String message = (employeeNames != null && !employeeNames.isBlank())
                ? String.format("Thanh toán thành công. Vui lòng đánh giá nhân viên (%s) cho booking %s.", employeeNames, bookingCode)
                : String.format("Thanh toán thành công. Vui lòng đánh giá nhân viên đã làm việc cho booking %s.", bookingCode);

        NotificationRequest request = new NotificationRequest(
                accountId,
                "CUSTOMER",
                Notification.NotificationType.REVIEW_REQUEST,
                "Đánh giá nhân viên",
                message,
                bookingId,
                Notification.RelatedEntityType.BOOKING,
                Notification.NotificationPriority.NORMAL,
                "/bookings/" + bookingId + "/review"
        );

        createNotification(request);
    }

    private void dispatchEmailNotification(Notification notification) {
        if (notification == null) {
            return;
        }
        emailRecipientResolver.resolveEmailByAccountId(notification.getAccountId())
                .ifPresentOrElse(
                        email -> emailService.sendNotificationEmail(email, notification),
                        () -> log.debug(
                                "Skip email for notification {} because account {} has no valid email",
                                notification.getNotificationId(),
                                notification.getAccountId()
                        )
                );
    }
}
