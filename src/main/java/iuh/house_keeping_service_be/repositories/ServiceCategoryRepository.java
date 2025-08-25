package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Integer> {

    @Query("SELECT sc FROM ServiceCategory sc WHERE sc.isActive = true ORDER BY sc.categoryName ASC")
    List<ServiceCategory> findAllActiveCategories();

    @Query("SELECT sc FROM ServiceCategory sc WHERE sc.categoryId = :categoryId AND sc.isActive = true")
    Optional<ServiceCategory> findActiveCategoryById(@Param("categoryId") Integer categoryId);

    @Query("SELECT COUNT(s) FROM Service s WHERE s.category.categoryId = :categoryId AND s.isActive = true")
    Long countActiveServicesByCategory(@Param("categoryId") Integer categoryId);
}