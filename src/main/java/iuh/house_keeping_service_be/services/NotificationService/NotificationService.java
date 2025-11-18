package iuh.house_keeping_service_be.services.NotificationService;

import iuh.house_keeping_service_be.dtos.Notification.NotificationRequest;
import iuh.house_keeping_service_be.dtos.Notification.NotificationResponse;
import iuh.house_keeping_service_be.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    
    /**
     * Create a new notification
     */
    NotificationResponse createNotification(NotificationRequest request);
    
    /**
     * Get all notifications for an account with pagination
     */
    Page<NotificationResponse> getNotificationsByAccountId(String accountId, Pageable pageable);
    
    /**
     * Get unread notifications for an account
     */
    Page<NotificationResponse> getUnreadNotifications(String accountId, Pageable pageable);
    
    /**
     * Get notification by ID
     */
    NotificationResponse getNotificationById(String notificationId);
    
    /**
     * Mark a notification as read
     */
    NotificationResponse markAsRead(String notificationId);
    
    /**
     * Mark all notifications as read for an account
     */
    int markAllAsRead(String accountId);
    
    /**
     * Get unread count for an account
     */
    long getUnreadCount(String accountId);
    
    /**
     * Delete a notification
     */
    void deleteNotification(String notificationId);
    
    /**
     * Delete old read notifications (cleanup job)
     */
    int cleanupOldNotifications(int daysOld);
    
    /**
     * Send booking created notification
     */
    void sendBookingCreatedNotification(String accountId, String bookingId, String bookingCode);
    
    /**
     * Send booking confirmed notification
     */
    void sendBookingConfirmedNotification(String accountId, String bookingId, String bookingCode);
    
    /**
     * Send booking cancelled notification
     */
    void sendBookingCancelledNotification(String accountId, String bookingId, String bookingCode, String reason);
    
    /**
     * Send booking verified notification
     */
    void sendBookingVerifiedNotification(String accountId, String bookingId, String bookingCode, boolean approved);
    
    /**
     * Send assignment created notification
     */
    void sendAssignmentCreatedNotification(String accountId, String assignmentId, String bookingCode);
    
    /**
     * Send assignment cancelled notification (crisis)
     */
    void sendAssignmentCancelledNotification(String accountId, String bookingId, String bookingCode, String reason);

    /**
     * Notify assigned employee when their assignment is cancelled
     */
    void sendAssignmentCancelledNotificationForEmployee(
            String accountId,
            String assignmentId,
            String bookingCode,
            String reason
    );

    /**
     * Send payment success notification
     */
    void sendPaymentSuccessNotification(String accountId, String paymentId, double amount);
    
    /**
     * Send review received notification
     */
    void sendReviewReceivedNotification(String accountId, String reviewId, int rating);
}
