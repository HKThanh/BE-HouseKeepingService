package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.RecurrenceType;
import iuh.house_keeping_service_be.enums.RecurringBookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for recurring booking schedules
 */
@Entity
@Table(name = "recurring_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBooking {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(name = "recurring_booking_id")
    private String recurringBookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private Employee assignedEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false)
    private RecurrenceType recurrenceType;

    // For WEEKLY: store days of week (1=Monday, 7=Sunday)
    // For MONTHLY: store days of month (1-31)
    @Column(name = "recurrence_days", nullable = false)
    private String recurrenceDays; // Comma-separated values: "1,3,5" or "1,15,30"

    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime; // Time of day for bookings

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate; // Null means indefinite

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "title", length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecurringBookingStatus status = RecurringBookingStatus.ACTIVE;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship with recurring booking details (services)
    @OneToMany(mappedBy = "recurringBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RecurringBookingDetail> recurringBookingDetails = new ArrayList<>();

    // Relationship with generated bookings
    @OneToMany(mappedBy = "recurringBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> generatedBookings = new ArrayList<>();

    // Helper methods
    public void addRecurringBookingDetail(RecurringBookingDetail detail) {
        recurringBookingDetails.add(detail);
        detail.setRecurringBooking(this);
    }

    public void addGeneratedBooking(Booking booking) {
        generatedBookings.add(booking);
        booking.setRecurringBooking(this);
    }
}
