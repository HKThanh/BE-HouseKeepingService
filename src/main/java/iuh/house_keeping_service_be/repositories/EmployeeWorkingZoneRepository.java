package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.models.EmployeeWorkingZone;
import iuh.house_keeping_service_be.models.EmployeeWorkingZoneId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeWorkingZoneRepository extends JpaRepository<EmployeeWorkingZone, EmployeeWorkingZoneId> {

    @Query("SELECT ewz FROM EmployeeWorkingZone ewz " +
            "JOIN FETCH ewz.employee " +
            "WHERE (:district IS NULL OR ewz.district = :district) " +
            "AND (:city IS NULL OR ewz.city = :city)")
    List<EmployeeWorkingZone> findByLocation(
            @Param("district") String district,
            @Param("city") String city
    );

    @Query("SELECT ewz FROM EmployeeWorkingZone ewz " +
            "WHERE ewz.employee.employeeId = :employeeId")
    List<EmployeeWorkingZone> findByEmployeeEmployeeId(@Param("employeeId") String employeeId);

    @Query("SELECT DISTINCT ewz.employee.employeeId FROM EmployeeWorkingZone ewz " +
            "WHERE (:district IS NULL OR ewz.district = :district) " +
            "AND (:city IS NULL OR ewz.city = :city)")
    List<String> findEmployeeIdsByLocation(
            @Param("district") String district,
            @Param("city") String city
    );

    boolean existsByEmployeeEmployeeIdAndDistrictAndCity(
            String employeeId,
            String district,
            String city
    );

    void deleteByEmployeeEmployeeId(String employeeId);

    @Query("SELECT COUNT(ewz) FROM EmployeeWorkingZone ewz WHERE ewz.employee.employeeId = :employeeId")
    long countByEmployeeId(@Param("employeeId") String employeeId);

    @Query("SELECT DISTINCT ewz.employee FROM EmployeeWorkingZone ewz WHERE ewz.district = :district AND ewz.city = :city")
    List<Employee> findEmployeesByDistrictAndCity(@Param("district") String district, @Param("city") String city);

    @Query("SELECT DISTINCT ewz.employee FROM EmployeeWorkingZone ewz WHERE ewz.city = :city")
    List<Employee> findEmployeesByCity(@Param("city") String city);

    @Query("SELECT ewz FROM EmployeeWorkingZone ewz WHERE ewz.employee.employeeId = :employeeId")
    List<EmployeeWorkingZone> findByEmployeeId(String employeeId);
}