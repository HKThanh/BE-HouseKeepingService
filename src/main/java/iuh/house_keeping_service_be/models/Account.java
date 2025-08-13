package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "account_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_accounts_users"))
    private User user;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "hashed_password", nullable = false, columnDefinition = "TEXT")
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "roles", nullable = false, length = 20)
    private Role roles;

    @Column(name = "status", length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    // Custom constructor excluding user_id for manual assignment if needed
    public Account(String username, String hashedPassword, Role roles, String status) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.roles = roles;
        this.status = status;
    }
}
