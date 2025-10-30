package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @Column(name = "notification_id", columnDefinition = "VARCHAR(36)")
    private String notificationId;
    
    @Column(name = "account_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String accountId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "related_id", columnDefinition = "VARCHAR(36)")
    private String relatedId; // bookingId, assignmentId, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(name = "related_type", length = 50)
    private RelatedEntityType relatedType; // BOOKING, ASSIGNMENT, PAYMENT, etc.
    
    @Column(name = "is_read", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    @Column(name = "action_url", length = 500)
    private String actionUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (notificationId == null) {
            notificationId = "ntf" + String.format("%05d", System.currentTimeMillis() % 100000) 
                           + "-0000-0000-0000-" + String.format("%012d", System.nanoTime() % 1000000000000L);
        }
        if (isRead == null) {
            isRead = false;
        }
        if (priority == null) {
            priority = NotificationPriority.NORMAL;
        }
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public enum NotificationType {
        BOOKING_CREATED,
        BOOKING_CONFIRMED,
        BOOKING_CANCELLED,
        BOOKING_COMPLETED,
        BOOKING_VERIFIED,
        BOOKING_REJECTED,
        ASSIGNMENT_CREATED,
        ASSIGNMENT_CANCELLED,
        ASSIGNMENT_CRISIS,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        REVIEW_RECEIVED,
        SYSTEM_ANNOUNCEMENT,
        PROMOTION_AVAILABLE
    }
    
    public enum RelatedEntityType {
        BOOKING,
        ASSIGNMENT,
        PAYMENT,
        REVIEW,
        PROMOTION,
        SYSTEM
    }
    
    public enum NotificationPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
