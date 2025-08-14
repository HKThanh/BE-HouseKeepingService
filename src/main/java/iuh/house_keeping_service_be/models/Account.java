package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {
    @Id
    @Size(max = 36)
    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 20)
    @Column(name = "role", length = 20)
    private String role;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    private AccountStatus status;

    @ColumnDefault("false")
    @Column(name = "is_admin")
    private Boolean isAdmin;

    @ColumnDefault("0")
    @Column(name = "admin_level")
    private Integer adminLevel;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_login")
    private Instant lastLogin;

}