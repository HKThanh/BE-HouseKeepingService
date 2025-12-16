package iuh.house_keeping_service_be.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @JsonIgnore
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_in_latitude")
    private Double checkInLatitude;

    @Column(name = "check_in_longitude")
    private Double checkInLongitude;

    @Column(name = "check_out_latitude")
    private Double checkOutLatitude;

    @Column(name = "check_out_longitude")
    private Double checkOutLongitude;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<BookingMedia> media = new ArrayList<>();

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

    // Helper methods for time range calculations
    public LocalDateTime getStartTime() {
        return bookingDetail.getBooking().getBookingTime();
    }

    public LocalDateTime getEndTime() {
        LocalDateTime startTime = getStartTime();
        BigDecimal duration = bookingDetail.getService().getEstimatedDurationHours();
        if (duration != null) {
            return startTime.plusHours(duration.longValue())
                    .plusMinutes((long) ((duration.remainder(BigDecimal.ONE).doubleValue() * 60)));
        }
        return startTime.plusHours(2); // Default 2 hours
    }
}