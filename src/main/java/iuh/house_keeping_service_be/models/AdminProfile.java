package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "admin_profile")
public class AdminProfile {
    @Id
    @Size(max = 36)
    @Column(name = "admin_profile_id", nullable = false, length = 36)
    private String adminProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id")
    private Account account;

    @Size(max = 100)
    @Column(name = "office_location", length = 100)
    private String officeLocation;

    @Size(max = 50)
    @Column(name = "department", length = 50)
    private String department;

    @Size(max = 255)
    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}