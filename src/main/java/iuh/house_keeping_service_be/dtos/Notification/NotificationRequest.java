package iuh.house_keeping_service_be.dtos.Notification;

import iuh.house_keeping_service_be.models.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotBlank(message = "Account ID không được để trống")
        String accountId,
        
        String targetRole,  // CUSTOMER, EMPLOYEE, ADMIN - Optional, for routing to specific role
        
        @NotNull(message = "Loại thông báo không được để trống")
        Notification.NotificationType type,
        
        @NotBlank(message = "Tiêu đề không được để trống")
        String title,
        
        @NotBlank(message = "Nội dung không được để trống")
        String message,
        
        String relatedId,
        
        Notification.RelatedEntityType relatedType,
        
        Notification.NotificationPriority priority,
        
        String actionUrl
) {
}
