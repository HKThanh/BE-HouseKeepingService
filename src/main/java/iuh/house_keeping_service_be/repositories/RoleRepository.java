package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(RoleName roleName);

    @Query("SELECT r FROM Role r WHERE r.roleName != 'ADMIN' ORDER BY r.roleId")
    List<Role> findNonAdminRoles();

    @Query("SELECT r FROM Role r ORDER BY r.roleId")
    List<Role> findAllOrderById();
}