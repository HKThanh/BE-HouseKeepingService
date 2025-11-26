package iuh.house_keeping_service_be.services.WebSocketNotificationService;

import iuh.house_keeping_service_be.dtos.Notification.NotificationWebSocketDTO;
import iuh.house_keeping_service_be.models.Notification;
import iuh.house_keeping_service_be.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending real-time notifications via WebSocket
 * Uses STOMP protocol to send messages to specific users
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    /**
     * Send notification to a specific user via WebSocket with role-based routing
     * User will receive message at /user/{accountId}/{role}/queue/notifications
     * 
     * @param accountId The account ID of the recipient
     * @param targetRole The specific role to receive notification (CUSTOMER, EMPLOYEE, ADMIN)
     * @param notification The notification entity to send
     */
    public void sendNotificationToUser(String accountId, String targetRole, Notification notification) {
        try {
            Long unreadCount = calculateUnreadCount(accountId);
            NotificationWebSocketDTO dto = NotificationWebSocketDTO.fromEntity(notification, unreadCount);
            
            // Build role-specific destination
            String destination = (targetRole != null && !targetRole.isEmpty())
                    ? String.format("/%s/queue/notifications", targetRole.toUpperCase())
                    : "/queue/notifications"; // Fallback to general queue if no role specified
            
            // Send to user-specific queue: /user/{accountId}/{ROLE}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                accountId,
                destination,
                dto
            );
            
            log.info("Sent WebSocket notification to user: {}, role: {}, type: {}", 
                    accountId, targetRole != null ? targetRole : "ALL", notification.getType());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user: {}, role: {}", 
                    accountId, targetRole, e);
        }
    }
    
    /**
     * Send notification to a specific user via WebSocket (without role filtering)
     * @deprecated Use sendNotificationToUser(accountId, targetRole, notification) instead
     */
    @Deprecated
    public void sendNotificationToUser(String accountId, Notification notification) {
        sendNotificationToUser(accountId, null, notification);
    }

    /**
     * Send notification to multiple users with role-based routing
     * 
     * @param accountIds List of account IDs to send to
     * @param targetRole The specific role to receive notification
     * @param notification The notification to send
     */
    public void sendNotificationToUsers(Iterable<String> accountIds, String targetRole, Notification notification) {
        accountIds.forEach(accountId -> sendNotificationToUser(accountId, targetRole, notification));
    }

    /**
     * Broadcast notification to all connected users
     * Message sent to /topic/notifications
     * 
     * @param notification The notification to broadcast
     */
    public void broadcastNotification(Notification notification) {
        try {
            NotificationWebSocketDTO dto = NotificationWebSocketDTO.fromEntity(notification);
            
            // Broadcast to all users subscribed to /topic/notifications
            messagingTemplate.convertAndSend("/topic/notifications", dto);
            
            log.info("Broadcasted WebSocket notification, type: {}", notification.getType());
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket notification", e);
        }
    }

    /**
     * Send notification with custom DTO
     * 
     * @param accountId The account ID of the recipient
     * @param dto The notification DTO to send
     */
    public void sendNotificationDTOToUser(String accountId, NotificationWebSocketDTO dto) {
        try {
            NotificationWebSocketDTO enrichedDto = dto.getUnreadCount() == null
                    ? NotificationWebSocketDTO.builder()
                        .notificationId(dto.getNotificationId())
                        .accountId(dto.getAccountId())
                        .targetRole(dto.getTargetRole())
                        .type(dto.getType())
                        .title(dto.getTitle())
                        .message(dto.getMessage())
                        .relatedId(dto.getRelatedId())
                        .relatedType(dto.getRelatedType())
                        .priority(dto.getPriority())
                        .actionUrl(dto.getActionUrl())
                        .createdAt(dto.getCreatedAt())
                        .unreadCount(calculateUnreadCount(accountId))
                        .build()
                    : dto;
            
            messagingTemplate.convertAndSendToUser(
                accountId,
                "/queue/notifications",
                enrichedDto
            );
            
            log.info("Sent custom WebSocket notification to user: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to send custom WebSocket notification to user: {}", accountId, e);
        }
    }

    private Long calculateUnreadCount(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return null;
        }
        try {
            return notificationRepository.countUnreadByAccountId(accountId);
        } catch (Exception ex) {
            log.warn("Failed to calculate unread count for account {}: {}", accountId, ex.getMessage());
            return null;
        }
    }
}
