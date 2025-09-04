package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ServiceOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOptionRepository extends JpaRepository<ServiceOption, Integer> {
    @Query("SELECT DISTINCT so FROM ServiceOption so " +
            "LEFT JOIN FETCH so.choices soc " +
            "WHERE so.service.serviceId = :serviceId " +
            "AND so.parentOption IS NULL " +
            "ORDER BY so.displayOrder ASC")
    List<ServiceOption> findByServiceIdWithChoices(@Param("serviceId") Integer serviceId);

}