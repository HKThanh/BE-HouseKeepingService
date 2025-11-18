package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;

/**
 * Entity for recurring booking service details
 */
@Entity
@Table(name = "recurring_booking_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingDetail {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(name = "recurring_booking_detail_id")
    private String recurringBookingDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_booking_id", nullable = false)
    private RecurringBooking recurringBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "price_per_unit", precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(name = "selected_choice_ids", columnDefinition = "TEXT")
    private String selectedChoiceIds; // Comma-separated IDs
}
