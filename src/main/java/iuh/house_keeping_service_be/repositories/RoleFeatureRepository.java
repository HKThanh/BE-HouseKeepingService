package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.RoleFeature;
import iuh.house_keeping_service_be.models.RoleFeatureId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleFeatureRepository extends JpaRepository<RoleFeature, RoleFeatureId> {

    @Query("SELECT rf FROM RoleFeature rf WHERE rf.role.roleId = :roleId")
    List<RoleFeature> findByRoleId(@Param("roleId") Integer roleId);

    @Query("SELECT rf FROM RoleFeature rf WHERE rf.role.roleId = :roleId AND rf.isEnabled = true")
    List<RoleFeature> findEnabledFeaturesByRoleId(@Param("roleId") Integer roleId);

    @Query("SELECT rf FROM RoleFeature rf WHERE rf.role.roleId = :roleId AND rf.feature.featureId = :featureId")
    Optional<RoleFeature> findByRoleIdAndFeatureId(@Param("roleId") Integer roleId, @Param("featureId") Integer featureId);

    @Query("SELECT f.featureName FROM RoleFeature rf JOIN rf.feature f WHERE rf.role.roleId = :roleId AND rf.isEnabled = true")
    List<String> findEnabledFeatureNamesByRoleId(@Param("roleId") Integer roleId);
}