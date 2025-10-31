package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    /**
     * Find all notifications for a specific account
     */
    Page<Notification> findByAccountIdOrderByCreatedAtDesc(String accountId, Pageable pageable);
    
    /**
     * Find unread notifications for a specific account
     */
    Page<Notification> findByAccountIdAndIsReadFalseOrderByCreatedAtDesc(String accountId, Pageable pageable);
    
    /**
     * Find notifications by type for a specific account
     */
    Page<Notification> findByAccountIdAndTypeOrderByCreatedAtDesc(
            String accountId, 
            Notification.NotificationType type, 
            Pageable pageable
    );
    
    /**
     * Count unread notifications for an account
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.accountId = :accountId AND n.isRead = false")
    long countUnreadByAccountId(@Param("accountId") String accountId);
    
    /**
     * Mark all notifications as read for an account
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.accountId = :accountId AND n.isRead = false")
    int markAllAsReadByAccountId(@Param("accountId") String accountId, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Delete old read notifications (cleanup)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find notifications by priority
     */
    List<Notification> findByAccountIdAndPriorityOrderByCreatedAtDesc(
            String accountId, 
            Notification.NotificationPriority priority
    );
    
    /**
     * Find notifications by related entity
     */
    List<Notification> findByRelatedIdAndRelatedType(
            String relatedId, 
            Notification.RelatedEntityType relatedType
    );
    
    /**
     * Find recent notifications (last N days)
     */
    @Query("SELECT n FROM Notification n WHERE n.accountId = :accountId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(
            @Param("accountId") String accountId, 
            @Param("since") LocalDateTime since
    );
}
