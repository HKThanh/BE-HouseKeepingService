package iuh.house_keeping_service_be.models;

    import iuh.house_keeping_service_be.enums.Rating;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.hibernate.annotations.GenericGenerator;

    import java.time.LocalDate;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "customer")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Customer {
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(name = "customer_id", length = 36)
        private String customerId;

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

        @Enumerated(EnumType.STRING)
        @Column(name = "rating", length = 10)
        private Rating rating;

        @Column(name = "vip_level")
        private Integer vipLevel;

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