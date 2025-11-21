package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.AdditionalFeeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_additional_fee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingAdditionalFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "fee_name", nullable = false, length = 255)
    private String feeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private AdditionalFeeType feeType;

    @Column(name = "fee_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal feeValue;

    @Column(name = "fee_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "is_system_surcharge", nullable = false)
    private boolean systemSurcharge;
}
