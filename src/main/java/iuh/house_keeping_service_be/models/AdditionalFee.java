package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.AdditionalFeeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "additional_fee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalFee {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private AdditionalFeeType feeType;

    @Column(name = "value", nullable = false, precision = 10, scale = 4)
    private BigDecimal value;

    @Column(name = "is_system_surcharge", nullable = false)
    private boolean systemSurcharge;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
