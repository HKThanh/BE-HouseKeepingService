package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(name = "chat_message_id", length = 36)
    private String chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private Account sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private ChatMessage parentMessage;

    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "payload_type")
    private String payloadType;

    @Column(name = "payload_data", columnDefinition = "TEXT")
    private String payloadData;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "read_by_account_id", length = 36)
    private String readByAccountId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_account_id", length = 36)
    private String deletedByAccountId;

    @Column(name = "recalled_at")
    private LocalDateTime recalledAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isRecalled() {
        return this.recalledAt != null;
    }

    public boolean isReply() {
        return this.parentMessage != null;
    }

    public void markDeleted(String accountId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedByAccountId = accountId;
    }

    public void markRecalled(String accountId) {
        LocalDateTime now = LocalDateTime.now();
        this.recalledAt = now;
        this.deletedAt = now;
        this.deletedByAccountId = accountId;
    }

    public boolean canBeRecalled(Duration recallWindow) {
        if (this.sentAt == null || recallWindow == null) {
            return false;
        }
        LocalDateTime deadline = this.sentAt.plus(recallWindow);
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(deadline) || now.isEqual(deadline);
    }
}