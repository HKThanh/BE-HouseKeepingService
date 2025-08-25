package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {

    @Query("SELECT s FROM Service s WHERE s.isActive = true ORDER BY s.name ASC")
    List<Service> findAllActiveServices();

    @Query("SELECT s FROM Service s WHERE s.serviceId = :serviceId AND s.isActive = true")
    Optional<Service> findActiveServiceById(@Param("serviceId") Integer serviceId);

    @Query("SELECT s FROM Service s WHERE s.isActive = true AND LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.name ASC")
    List<Service> searchActiveServicesByName(@Param("keyword") String keyword);

    @Query("SELECT COUNT(s) FROM Service s WHERE s.isActive = true")
    Long countActiveServices();

    @Query("SELECT s FROM Service s WHERE s.category.categoryId = :categoryId AND s.isActive = true ORDER BY s.name ASC")
    List<Service> findActiveServicesByCategory(@Param("categoryId") Integer categoryId);
}