package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer extends User{
    @Id
    @Column(name = "customer_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_male")
    private Gender gender;

    @Column(name = "address")
    private String address;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;
}
