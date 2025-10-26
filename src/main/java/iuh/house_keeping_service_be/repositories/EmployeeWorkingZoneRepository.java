package iuh.house_keeping_service_be.repositories;

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
            "WHERE (:ward IS NULL OR ewz.ward = :ward) " +
            "AND (:city IS NULL OR ewz.city = :city)")
    List<EmployeeWorkingZone> findByLocation(
            @Param("ward") String ward,
            @Param("city") String city
    );

    @Query("SELECT ewz FROM EmployeeWorkingZone ewz " +
            "JOIN FETCH ewz.employee " +
            "WHERE (:ward IS NULL OR LOWER(ewz.ward) LIKE LOWER(CONCAT('%', :ward, '%'))) " +
            "AND (:city IS NULL OR LOWER(ewz.city) LIKE LOWER(CONCAT('%', :city, '%')))")
    List<EmployeeWorkingZone> findByLocationContaining(
            @Param("ward") String ward,
            @Param("city") String city
    );
}