package iuh.house_keeping_service_be.dtos.Notification;

import iuh.house_keeping_service_be.models.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for sending notifications via WebSocket
 * Contains essential notification info for real-time updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationWebSocketDTO {
    
    private String notificationId;
    private String accountId;
    private String targetRole;  // CUSTOMER, EMPLOYEE, ADMIN
    private String type;
    private String title;
    private String message;
    private String relatedId;
    private String relatedType;
    private String priority;
    private String actionUrl;
    private LocalDateTime createdAt;
    
    public static NotificationWebSocketDTO fromEntity(Notification notification) {
        return NotificationWebSocketDTO.builder()
                .notificationId(notification.getNotificationId())
                .accountId(notification.getAccountId())
                .targetRole(notification.getTargetRole())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType() != null ? notification.getRelatedType().name() : null)
                .priority(notification.getPriority().name())
                .actionUrl(notification.getActionUrl())
                .createdAt(notification.getCreatedAt())
                .build();
    }
    
    public static NotificationWebSocketDTO fromResponse(NotificationResponse response) {
        return NotificationWebSocketDTO.builder()
                .notificationId(response.notificationId())
                .accountId(response.accountId())
                .targetRole(response.targetRole())
                .type(response.type().name())
                .title(response.title())
                .message(response.message())
                .relatedId(response.relatedId())
                .relatedType(response.relatedType() != null ? response.relatedType().name() : null)
                .priority(response.priority().name())
                .actionUrl(response.actionUrl())
                .createdAt(response.createdAt())
                .build();
    }
}
