package iuh.house_keeping_service_be.models;

import iuh.house_keeping_service_be.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "uuid2")
    @Column(name = "assignment_id")
    private String assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_detail_id", nullable = false)
    private BookingDetail bookingDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Helper method to get booking time from booking detail
    public LocalDateTime getBookingTime() {
        return bookingDetail != null && bookingDetail.getBooking() != null
            ? bookingDetail.getBooking().getBookingTime()
            : null;
    }

    // Helper method to get estimated duration
    public Double getEstimatedDuration() {
        return bookingDetail != null && bookingDetail.getService() != null
            ? bookingDetail.getService().getEstimatedDurationHours().doubleValue()
            : null;
    }
}