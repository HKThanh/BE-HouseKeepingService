package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.RuleCondition;
import iuh.house_keeping_service_be.models.RuleConditionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RuleConditionRepository extends JpaRepository<RuleCondition, RuleConditionId> {
    @Query("SELECT rc.choice.id FROM RuleCondition rc WHERE rc.rule.id = :ruleId")
    List<Integer> findChoiceIdsByRuleId(@Param("ruleId") Integer ruleId);
}
