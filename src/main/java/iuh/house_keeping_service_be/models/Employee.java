package iuh.house_keeping_service_be.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.enums.Rating;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

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
    @JsonIgnore
    private Account account;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "is_male")
    private Boolean isMale;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "hired_date")
    private LocalDate hiredDate;

    @JdbcTypeCode(SqlTypes.ARRAY) // Giúp Hibernate hiểu đây là kiểu mảng text của Postgres
    @Column(name = "skills", columnDefinition = "text[]")
    private List<String> skills;

    @Column(name = "bio", columnDefinition = "TEXT[]")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", length = 10)
    private Rating rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_status", length = 20)
    private EmployeeStatus employeeStatus = EmployeeStatus.AVAILABLE;

    // Relationships
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EmployeeUnavailability> unavailabilities;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnore
    private List<EmployeeWorkingZone> workingZones;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EmployeeWorkingHours> workingHours;
}