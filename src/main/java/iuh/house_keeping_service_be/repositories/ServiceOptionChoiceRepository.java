package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.ServiceOptionChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOptionChoiceRepository extends JpaRepository<ServiceOptionChoice, Integer> {
    
    // Find by option ID ordered by display order
    @Query("SELECT soc FROM ServiceOptionChoice soc WHERE soc.option.id = :optionId ORDER BY soc.displayOrder")
    List<ServiceOptionChoice> findByServiceOptionIdOrderByDisplayOrder(@Param("optionId") Integer optionId);
    
    // Find by choice IDs
    @Query("SELECT soc FROM ServiceOptionChoice soc WHERE soc.id IN :choiceIds")
    List<ServiceOptionChoice> findByIdIn(@Param("choiceIds") List<Integer> choiceIds);
    
    // Find choices for a service
    @Query("SELECT soc FROM ServiceOptionChoice soc " +
           "JOIN soc.option so " +
           "WHERE so.service.serviceId = :serviceId " +
           "ORDER BY so.displayOrder, soc.displayOrder")
    List<ServiceOptionChoice> findByServiceId(@Param("serviceId") Integer serviceId);
    
    // Validate choice IDs for a service
    @Query("SELECT soc.id FROM ServiceOptionChoice soc " +
           "JOIN soc.option so " +
           "WHERE so.service.serviceId = :serviceId AND soc.id IN :choiceIds")
    List<Integer> validateChoiceIdsForService(@Param("serviceId") Integer serviceId,
                                             @Param("choiceIds") List<Integer> choiceIds);
    
    // Get choice pricing information with service option details
    @Query("SELECT soc FROM ServiceOptionChoice soc " +
           "LEFT JOIN FETCH soc.option so " +
           "WHERE soc.id IN :choiceIds " +
           "ORDER BY so.displayOrder, soc.displayOrder")
    List<ServiceOptionChoice> findChoicesWithPricing(@Param("choiceIds") List<Integer> choiceIds);
    
    // Count choices for an option
    @Query("SELECT COUNT(soc) FROM ServiceOptionChoice soc WHERE soc.option.id = :optionId")
    long countByServiceOptionId(@Param("optionId") Integer optionId);
}