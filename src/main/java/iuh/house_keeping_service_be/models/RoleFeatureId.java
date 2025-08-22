package iuh.house_keeping_service_be.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleFeatureId implements Serializable {

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "feature_id")
    private Integer featureId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleFeatureId that = (RoleFeatureId) o;
        return Objects.equals(roleId, that.roleId) &&
               Objects.equals(featureId, that.featureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, featureId);
    }
}