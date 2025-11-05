package iuh.house_keeping_service_be.models;

    import iuh.house_keeping_service_be.enums.Rating;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

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

        @Column(name = "birthdate")
        private LocalDate birthdate;

        @Enumerated(EnumType.STRING)
        @Column(name = "rating", length = 10)
        private Rating rating;

        @Column(name = "vip_level")
        private Integer vipLevel;
    }