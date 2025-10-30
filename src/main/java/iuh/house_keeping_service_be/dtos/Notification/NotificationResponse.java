package iuh.house_keeping_service_be.dtos.Notification;

import iuh.house_keeping_service_be.models.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        String notificationId,
        String accountId,
        Notification.NotificationType type,
        String title,
        String message,
        String relatedId,
        Notification.RelatedEntityType relatedType,
        Boolean isRead,
        LocalDateTime readAt,
        Notification.NotificationPriority priority,
        String actionUrl,
        LocalDateTime createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getAccountId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedId(),
                notification.getRelatedType(),
                notification.getIsRead(),
                notification.getReadAt(),
                notification.getPriority(),
                notification.getActionUrl(),
                notification.getCreatedAt()
        );
    }
}
