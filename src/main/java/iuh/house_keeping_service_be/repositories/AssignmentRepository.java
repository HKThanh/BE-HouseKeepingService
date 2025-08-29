package iuh.house_keeping_service_be.repositories;

  import iuh.house_keeping_service_be.models.Assignment;
  import iuh.house_keeping_service_be.enums.AssignmentStatus;
  import org.springframework.data.jpa.repository.JpaRepository;
  import org.springframework.data.jpa.repository.Query;
  import org.springframework.data.repository.query.Param;
  import org.springframework.stereotype.Repository;

  import java.time.LocalDateTime;
  import java.util.List;

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
  }