package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.EmployeeWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeWorkingHoursRepository extends JpaRepository<EmployeeWorkingHours, String> {

    /**
     * Find all working hours for an employee
     */
    List<EmployeeWorkingHours> findByEmployee_EmployeeId(String employeeId);

    /**
     * Find working hours for a specific employee and day
     */
    Optional<EmployeeWorkingHours> findByEmployee_EmployeeIdAndDayOfWeek(String employeeId, DayOfWeek dayOfWeek);

    /**
     * Find all employees' working hours for a specific day
     */
    List<EmployeeWorkingHours> findByDayOfWeekAndIsWorkingDayTrue(DayOfWeek dayOfWeek);

    /**
     * Check if an employee is working on a specific day
     */
    @Query("SELECT CASE WHEN COUNT(wh) > 0 THEN true ELSE false END FROM EmployeeWorkingHours wh " +
           "WHERE wh.employee.employeeId = :employeeId " +
           "AND wh.dayOfWeek = :dayOfWeek " +
           "AND wh.isWorkingDay = true")
    boolean isEmployeeWorkingOnDay(@Param("employeeId") String employeeId, 
                                   @Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * Check if a time range is within employee's working hours for a specific day
     */
    @Query("SELECT wh FROM EmployeeWorkingHours wh " +
           "WHERE wh.employee.employeeId = :employeeId " +
           "AND wh.dayOfWeek = :dayOfWeek " +
           "AND wh.isWorkingDay = true " +
           "AND wh.startTime <= :startTime " +
           "AND wh.endTime >= :endTime")
    Optional<EmployeeWorkingHours> findWorkingHoursContainingTimeRange(
            @Param("employeeId") String employeeId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Find employees who are working at a specific day and time range
     */
    @Query("SELECT DISTINCT wh.employee.employeeId FROM EmployeeWorkingHours wh " +
           "WHERE wh.dayOfWeek = :dayOfWeek " +
           "AND wh.isWorkingDay = true " +
           "AND wh.startTime <= :startTime " +
           "AND wh.endTime >= :endTime " +
           "AND (wh.breakStartTime IS NULL OR :startTime >= wh.breakEndTime OR :endTime <= wh.breakStartTime)")
    List<String> findEmployeesAvailableAtTimeRange(
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Delete all working hours for an employee
     */
    void deleteByEmployee_EmployeeId(String employeeId);

    /**
     * Check if working hours exist for an employee
     */
    boolean existsByEmployee_EmployeeId(String employeeId);

    /**
     * Find working days for an employee (days where isWorkingDay = true)
     */
    @Query("SELECT wh FROM EmployeeWorkingHours wh " +
           "WHERE wh.employee.employeeId = :employeeId " +
           "AND wh.isWorkingDay = true " +
           "ORDER BY wh.dayOfWeek")
    List<EmployeeWorkingHours> findWorkingDaysByEmployeeId(@Param("employeeId") String employeeId);
}
