package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.EmployeeUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmployeeUnavailabilityRepository extends JpaRepository<EmployeeUnavailability, String> {

    @Query("SELECT eu FROM EmployeeUnavailability eu WHERE eu.employee.employeeId = :employeeId " +
           "AND eu.isApproved = true " +
           "AND eu.endTime >= :startTime AND eu.startTime <= :endTime " +
           "ORDER BY eu.startTime ASC")
    List<EmployeeUnavailability> findByEmployeeAndPeriod(
            @Param("employeeId") String employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT eu FROM EmployeeUnavailability eu WHERE eu.employee.employeeId = :employeeId " +
           "AND eu.isApproved = true " +
           "AND eu.endTime >= :currentTime " +
           "ORDER BY eu.startTime ASC")
    List<EmployeeUnavailability> findFutureByEmployee(
            @Param("employeeId") String employeeId,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Query("SELECT COUNT(eu) > 0 FROM EmployeeUnavailability eu WHERE eu.employee.employeeId = :employeeId " +
           "AND eu.isApproved = true " +
           "AND eu.startTime < :endTime AND eu.endTime > :startTime")
    boolean hasConflict(
            @Param("employeeId") String employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<EmployeeUnavailability> findByEmployeeEmployeeIdOrderByStartTimeAsc(String employeeId);
}