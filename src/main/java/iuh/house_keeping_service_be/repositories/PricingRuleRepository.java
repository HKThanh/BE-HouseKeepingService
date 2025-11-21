package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Integer> {
    @Query("SELECT pr FROM PricingRule pr WHERE pr.service.serviceId = :serviceId ORDER BY pr.priority DESC")
    List<PricingRule> findByServiceIdOrderByPriorityDesc(@Param("serviceId") Integer serviceId);
    
    @Query("SELECT pr FROM PricingRule pr " +
           "JOIN RuleCondition rc ON rc.rule.id = pr.id " +
           "WHERE rc.choice.id = :choiceId AND pr.isActive = true")
    List<PricingRule> findPricingRulesByChoiceId(@Param("choiceId") Integer choiceId);
}