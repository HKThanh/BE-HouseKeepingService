package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Assignment;
import iuh.house_keeping_service_be.enums.AssignmentStatus;
import iuh.house_keeping_service_be.repositories.projections.ZoneCoordinate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {

    @Query("SELECT a FROM Assignment a " +
            "JOIN FETCH a.bookingDetail bd " +
            "JOIN FETCH bd.booking b " +
            "JOIN FETCH bd.service s " +
            "WHERE a.employee.employeeId = :employeeId " +
            "AND b.bookingTime >= :startTime AND b.bookingTime <= :endTime " +
            "AND a.status IN :statuses " +
            "ORDER BY b.bookingTime ASC")
    List<Assignment> findByEmployeeAndPeriodWithStatuses(
            @Param("employeeId") String employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<AssignmentStatus> statuses
    );

    @Query("SELECT a FROM Assignment a " +
            "JOIN FETCH a.bookingDetail bd " +
            "JOIN FETCH bd.booking b " +
            "WHERE a.employee.employeeId = :employeeId " +
            "AND a.status IN (iuh.house_keeping_service_be.enums.AssignmentStatus.ASSIGNED, iuh.house_keeping_service_be.enums.AssignmentStatus.IN_PROGRESS) " +
            "AND b.bookingTime >= :currentTime " +
            "ORDER BY b.bookingTime ASC")
    List<Assignment> findActiveByEmployee(
            @Param("employeeId") String employeeId,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Assignment a " +
            "JOIN a.bookingDetail bd " +
            "JOIN bd.booking b " +
            "JOIN bd.service s " +
            "WHERE a.employee.employeeId = :employeeId " +
            "AND a.status IN (iuh.house_keeping_service_be.enums.AssignmentStatus.ASSIGNED, iuh.house_keeping_service_be.enums.AssignmentStatus.IN_PROGRESS) " +
            "AND b.bookingTime < :endTime " +
            "AND b.bookingTime + s.estimatedDurationHours HOUR > :startTime")
    boolean hasActiveAssignmentConflict(
            @Param("employeeId") String employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Assignment> findByEmployeeEmployeeIdOrderByCreatedAtDesc(String employeeId);

    @Query("SELECT a FROM Assignment a WHERE a.employee.employeeId = :employeeId AND a.status IN :statuses AND " +
            "((a.checkInTime BETWEEN :startTime AND :endTime) OR (a.checkOutTime BETWEEN :startTime AND :endTime) OR " +
            "(a.checkInTime <= :startTime AND a.checkOutTime >= :endTime))")
    List<Assignment> findActiveAssignmentsByEmployeeAndTimeRange(
            @Param("employeeId") String employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<AssignmentStatus> statuses);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.employee.employeeId = :employeeId AND a.status = 'COMPLETED'")
    Integer countCompletedJobsByEmployee(@Param("employeeId") String employeeId);

    // Find by employee ID
    @Query("SELECT a FROM Assignment a WHERE a.employee.employeeId = :employeeId ORDER BY a.createdAt DESC")
    List<Assignment> findByEmployeeIdOrderByCreatedAtDesc(@Param("employeeId") String employeeId);

    // Find by booking detail ID
    @Query("SELECT a FROM Assignment a WHERE a.bookingDetail.id = :bookingDetailId")
    List<Assignment> findByBookingDetailId(@Param("bookingDetailId") String bookingDetailId);

    // Find by status
    List<Assignment> findByStatusOrderByCreatedAtDesc(AssignmentStatus status);

    boolean existsByBookingDetailIdAndEmployeeEmployeeId(String bookingDetailId, String employeeId);

    @Query(value = "SELECT a.* FROM assignments a " +
            "JOIN booking_details bd ON a.booking_detail_id = bd.booking_detail_id " +
            "JOIN bookings b ON bd.booking_id = b.booking_id " +
            "JOIN service s ON bd.service_id = s.service_id " +
            "WHERE a.employee_id = :employeeId " +
            "AND a.status NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND b.booking_time < :endTime " +
            "AND (b.booking_time + (s.estimated_duration_hours * INTERVAL '1 hour')) > :startTime",
            nativeQuery = true)
    List<Assignment> findConflictingAssignments(@Param("employeeId") String employeeId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    // Get employee workload for specific date
    @Query("SELECT COUNT(a) FROM Assignment a " +
            "WHERE a.employee.employeeId = :employeeId " +
            "AND DATE(a.bookingDetail.booking.bookingTime) = DATE(:date) " +
            "AND a.status NOT IN (iuh.house_keeping_service_be.enums.AssignmentStatus.CANCELLED)")
    long getEmployeeWorkloadForDate(@Param("employeeId") String employeeId,
                                    @Param("date") LocalDateTime date);

    // Find assignments in date range
    @Query("SELECT a FROM Assignment a " +
            "WHERE a.bookingDetail.booking.bookingTime BETWEEN :startDate AND :endDate " +
            "ORDER BY a.bookingDetail.booking.bookingTime")
    List<Assignment> findAssignmentsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // Get employee assignment statistics
    @Query("SELECT a.status, COUNT(a) FROM Assignment a " +
            "WHERE a.employee.employeeId = :employeeId " +
            "GROUP BY a.status")
    List<Object[]> getEmployeeAssignmentStats(@Param("employeeId") String employeeId);

    // Find assignments with employee and booking details
    @Query("SELECT a FROM Assignment a " +
            "LEFT JOIN FETCH a.employee e " +
            "LEFT JOIN FETCH a.bookingDetail bd " +
            "LEFT JOIN FETCH bd.booking b " +
            "LEFT JOIN FETCH bd.service s " +
            "WHERE a.bookingDetail.id = :bookingDetailId")
    List<Assignment> findAssignmentsWithDetailsByBookingDetail(@Param("bookingDetailId") String bookingDetailId);

    @Query("SELECT a FROM Assignment a " +
            "LEFT JOIN FETCH a.bookingDetail bd " +
            "LEFT JOIN FETCH bd.booking b " +
            "LEFT JOIN FETCH bd.service s " +
            "LEFT JOIN FETCH b.customer c " +
            "LEFT JOIN FETCH b.address " +
            "LEFT JOIN FETCH c.account " +
            "WHERE a.employee.employeeId = :employeeId " +
            "AND a.status = :status " +
            "ORDER BY a.createdAt DESC")
    List<Assignment> findByEmployeeIdAndStatusWithDetails(
            @Param("employeeId") String employeeId,
            @Param("status") AssignmentStatus status,
            Pageable pageable);

    @Query("SELECT a FROM Assignment a " +
            "LEFT JOIN FETCH a.bookingDetail bd " +
            "LEFT JOIN FETCH bd.booking b " +
            "LEFT JOIN FETCH bd.service s " +
            "LEFT JOIN FETCH b.customer c " +
            "LEFT JOIN FETCH b.address " +
            "LEFT JOIN FETCH c.account " +
            "WHERE a.employee.employeeId = :employeeId " +
            "ORDER BY a.createdAt DESC")
    List<Assignment> findByEmployeeIdWithDetails(
            @Param("employeeId") String employeeId,
            Pageable pageable);

    @Query("SELECT a FROM Assignment a " +
            "WHERE a.bookingDetail.booking.bookingId = :bookingId")
    List<Assignment> findByBookingIdWithStatus(@Param("bookingId") String bookingId);


    @Query("SELECT a FROM Assignment a " +
            "LEFT JOIN FETCH a.bookingDetail bd " +
            "LEFT JOIN FETCH bd.booking b " +
            "LEFT JOIN FETCH bd.service s " +
            "LEFT JOIN FETCH a.employee e " +
            "LEFT JOIN FETCH b.customer c " +
            "LEFT JOIN FETCH c.account " +
            "LEFT JOIN FETCH b.address " +
            "WHERE a.assignmentId = :assignmentId")
    Optional<Assignment> findByIdWithDetails(@Param("assignmentId") String assignmentId);

    boolean existsByBookingDetail_Booking_BookingIdAndEmployee_EmployeeId(String bookingId, String employeeId);

    boolean existsByBookingDetail_Booking_BookingIdAndEmployee_EmployeeIdAndStatusIn(
            String bookingId,
            String employeeId,
            Iterable<AssignmentStatus> statuses);
}