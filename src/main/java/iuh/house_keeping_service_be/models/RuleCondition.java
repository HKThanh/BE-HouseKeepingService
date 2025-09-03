package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "rule_conditions")
public class RuleCondition {
    @SequenceGenerator(name = "rule_conditions_id_gen", sequenceName = "pricing_rules_rule_id_seq", allocationSize = 1)
    @EmbeddedId
    private RuleConditionId id;

    @MapsId("ruleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "rule_id", nullable = false)
    private PricingRule rule;

    @MapsId("choiceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "choice_id", nullable = false)
    private ServiceOptionChoice choice;

}