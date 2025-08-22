package iuh.house_keeping_service_be.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleFeature {

    @EmbeddedId
    private RoleFeatureId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("featureId")
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;
}