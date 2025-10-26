package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfile {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "admin_profile_id", length = 36)
    private String adminProfileId;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "is_male")
    private Boolean isMale;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "hire_date")
    private LocalDate hireDate;
}