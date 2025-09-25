package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.EmployeeStatus;
import iuh.house_keeping_service_be.models.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByAccount_AccountId(String accountId);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByAccount_PhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByAccount_PhoneNumber(String phoneNumber);

    List<Employee> findByEmployeeStatus(EmployeeStatus employeeStatus);

    // Find available employees for service and location (PostgreSQL optimized)
//    @Query("SELECT DISTINCT e FROM Employee e " +
//           "JOIN e.workingZones wz " +
//           "WHERE e.employeeStatus = 'AVAILABLE' " +
//           "AND wz.district = :district " +
//           "AND wz.city = :city " +
//           "AND NOT EXISTS (" +
//           "   SELECT 1 FROM Assignment a " +
//           "   WHERE a.employee.id = e.id " +
//           "   AND a.status NOT IN ('CANCELLED', 'COMPLETED') " +
//           "   AND a.bookingDetail.booking.bookingTime < :endTime " +
//           "   AND (a.bookingDetail.booking.bookingTime + (a.bookingDetail.service.estimatedDurationHours || ' hours')::INTERVAL) > :startTime" +
//           ") " +
//           "AND NOT EXISTS (" +
//           "   SELECT 1 FROM EmployeeUnavailability eu " +
//           "   WHERE eu.employee.id = e.id " +
//           "   AND :bookingTime BETWEEN eu.startTime AND eu.endTime" +
//           ")")
//    List<Employee> findAvailableEmployees(@Param("district") String district,
//                                         @Param("city") String city,
//                                         @Param("bookingTime") LocalDateTime bookingTime,
//                                         @Param("startTime") LocalDateTime startTime,
//                                         @Param("endTime") LocalDateTime endTime);

//    @Query("SELECT DISTINCT e FROM Employee e " +
//            "INNER JOIN e.workingZones wz " +
//            "WHERE e.employeeStatus = 'AVAILABLE' " +
//            "AND wz.district = :district " +
//            "AND wz.city = :city " +
//            "AND NOT EXISTS (" +
//            "   SELECT 1 FROM Assignment a " +
//            "   INNER JOIN a.employee emp " +
//            "   INNER JOIN a.bookingDetail bd " +
//            "   INNER JOIN bd.booking b " +
//            "   INNER JOIN bd.service s " +
//            "   WHERE emp.id = e.id " +
//            "   AND a.status NOT IN ('CANCELLED', 'COMPLETED') " +
//            "   AND b.bookingTime < :endTime " +
//            "   AND (b.bookingTime + (COALESCE(s.estimatedDurationHours, 0) * INTERVAL '1 hour')) > :startTime " +
//            ") " +
//            "AND NOT EXISTS (" +
//            "   SELECT 1 FROM EmployeeUnavailability eu " +
//            "   INNER JOIN eu.employee emp " +
//            "   WHERE emp.id = e.id " +
//            "   AND :bookingTime BETWEEN eu.startTime AND eu.endTime " +
//            ")")
//    List<Employee> findAvailableEmployees(@Param("district") String district,
//                                          @Param("city") String city,
//                                          @Param("bookingTime") LocalDateTime bookingTime,
//                                          @Param("startTime") LocalDateTime startTime,
//                                          @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT DISTINCT e.* FROM Employee e " +
            "INNER JOIN employee_working_zones wz ON wz.employee_id = e.employee_id " +
            "WHERE e.employee_status = 'AVAILABLE' " +
            "AND (:ward IS NULL OR wz.ward = :ward) " +
            "AND (:city IS NULL OR wz.city = :city) " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM assignments a " +
            "   INNER JOIN Employee emp ON a.employee_id = emp.employee_id " +
            "   INNER JOIN booking_details bd ON a.booking_detail_id = bd.booking_detail_id " +
            "   INNER JOIN bookings b ON bd.booking_id = b.booking_id " +
            "   INNER JOIN Service s ON bd.service_id = s.service_id " +
            "   WHERE emp.employee_id = e.employee_id " +
            "   AND a.status NOT IN ('CANCELLED', 'COMPLETED') " +
            "   AND b.booking_time < :endTime " +
            "   AND (b.booking_time + (COALESCE(s.estimated_duration_hours, 0) * INTERVAL '1 hour')) > :startTime " +
            ") " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM employee_unavailability eu " +
            "   INNER JOIN Employee emp ON eu.employee_id = emp.employee_id " +
            "   WHERE emp.employee_id = e.employee_id " +
            "   AND :bookingTime BETWEEN eu.start_time AND eu.end_time " +
            ")", nativeQuery = true)
    List<Employee> findAvailableEmployees(@Param("ward") String ward,
                                          @Param("city") String city,
                                          @Param("bookingTime") LocalDateTime bookingTime,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
    
    // Find employee with account and working zones
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.account " +
           "LEFT JOIN FETCH e.workingZones " +
           "WHERE e.employeeId = :employeeId")
    Optional<Employee> findEmployeeWithDetails(@Param("employeeId") String employeeId);

    // Search and filter employees with pagination
    Page<Employee> findAll(Pageable pageable);
}