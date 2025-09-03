package iuh.house_keeping_service_be.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class RuleConditionId implements Serializable {
    @Serial
    private static final long serialVersionUID = -4506193951659517019L;
    @NotNull
    @Column(name = "rule_id", nullable = false)
    private Integer ruleId;

    @NotNull
    @Column(name = "choice_id", nullable = false)
    private Integer choiceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RuleConditionId entity = (RuleConditionId) o;
        return Objects.equals(this.choiceId, entity.choiceId) &&
                Objects.equals(this.ruleId, entity.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(choiceId, ruleId);
    }

}