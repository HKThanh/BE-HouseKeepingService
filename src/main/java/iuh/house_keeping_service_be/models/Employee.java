package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.enums.Rating;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "employee_id", length = 36)
    private String employeeId;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "is_male")
    private Boolean isMale;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "hired_date")
    private LocalDate hiredDate;

    @ElementCollection
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "skill")
    private List<String> skills;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", length = 10)
    private Rating rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_status", length = 20)
    private EmployeeStatus employeeStatus = EmployeeStatus.AVAILABLE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}