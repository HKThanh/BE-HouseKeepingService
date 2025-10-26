package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.PaymentMethodCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "method_id")
    private Integer methodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_code", nullable = false, unique = true)
    private PaymentMethodCode methodCode;

    @Column(name = "method_name", nullable = false)
    private String methodName;

//    @Column(name = "icon_url")
//    private String iconUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "description")
    private String description;
}