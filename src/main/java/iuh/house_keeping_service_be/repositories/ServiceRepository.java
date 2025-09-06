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

        // Find active services ordered by name
    List<Service> findByIsActiveTrueOrderByName();
    
    // Find by category
    @Query("SELECT s FROM Service s WHERE s.category.categoryId = :categoryId AND s.isActive = true ORDER BY s.name")
    List<Service> findByCategoryIdAndIsActiveOrderByName(@Param("categoryId") Integer categoryId);
    
    // Find service with options and choices
    @Query("SELECT DISTINCT s FROM Service s " +
           "LEFT JOIN FETCH s.serviceOptions so " +
           "LEFT JOIN FETCH so.choices soc " +
           "WHERE s.id = :serviceId AND s.isActive = true " +
           "ORDER BY so.displayOrder, soc.displayOrder")
    Optional<Service> findServiceWithOptions(@Param("serviceId") Integer serviceId);
    
    // Find services by IDs
    @Query("SELECT s FROM Service s WHERE s.serviceId IN :serviceIds AND s.isActive = true")
    List<Service> findActiveServicesByIds(@Param("serviceIds") List<Integer> serviceIds);
    
    // Check service availability for booking
    @Query("SELECT s FROM Service s WHERE s.serviceId = :serviceId AND s.isActive = true")
    Optional<Service> findBookableService(@Param("serviceId") Integer serviceId);
    
    // Get popular services with booking count
    @Query("SELECT s, COUNT(bd) as bookingCount FROM Service s " +
           "LEFT JOIN BookingDetail bd ON s.serviceId = bd.service.serviceId " +
           "WHERE s.isActive = true " +
           "GROUP BY s.serviceId " +
           "ORDER BY bookingCount DESC")
    List<Object[]> findPopularServices();
    
    // Find service with category information
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.category WHERE s.serviceId = :serviceId")
    Optional<Service> findServiceWithCategory(@Param("serviceId") Integer serviceId);
}