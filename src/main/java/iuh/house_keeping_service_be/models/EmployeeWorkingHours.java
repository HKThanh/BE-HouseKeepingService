package iuh.house_keeping_service_be.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Entity representing employee working hours configuration.
 * Each record defines the working hours for an employee on a specific day of the week.
 */
@Entity
@Table(name = "employee_working_hours", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "day_of_week"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWorkingHours {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "working_hours_id", length = 36)
    private String workingHoursId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Builder.Default
    @Column(name = "is_working_day", nullable = false)
    private Boolean isWorkingDay = true;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    /**
     * Check if a given time is within the employee's working hours for this day
     */
    public boolean isWithinWorkingHours(LocalTime time) {
        if (!isWorkingDay) {
            return false;
        }
        
        boolean withinMainHours = !time.isBefore(startTime) && !time.isAfter(endTime);
        
        // Check if time is during break
        if (breakStartTime != null && breakEndTime != null) {
            boolean duringBreak = !time.isBefore(breakStartTime) && !time.isAfter(breakEndTime);
            return withinMainHours && !duringBreak;
        }
        
        return withinMainHours;
    }

    /**
     * Check if a time range is fully within working hours
     */
    public boolean isTimeRangeWithinWorkingHours(LocalTime rangeStart, LocalTime rangeEnd) {
        if (!isWorkingDay) {
            return false;
        }
        
        boolean startWithin = !rangeStart.isBefore(startTime) && !rangeStart.isAfter(endTime);
        boolean endWithin = !rangeEnd.isBefore(startTime) && !rangeEnd.isAfter(endTime);
        
        if (!startWithin || !endWithin) {
            return false;
        }
        
        // Check if range overlaps with break time
        if (breakStartTime != null && breakEndTime != null) {
            boolean overlapsBreak = rangeStart.isBefore(breakEndTime) && rangeEnd.isAfter(breakStartTime);
            return !overlapsBreak;
        }
        
        return true;
    }
}
