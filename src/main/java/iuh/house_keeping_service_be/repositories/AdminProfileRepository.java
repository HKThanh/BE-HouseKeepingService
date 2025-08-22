package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.AdminProfile;
import iuh.house_keeping_service_be.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminProfileRepository extends JpaRepository<AdminProfile, String> {
    Optional<AdminProfile> findByAccount_AccountId(String accountId);

    @Query("SELECT ap FROM AdminProfile ap JOIN ap.account.roles r WHERE ap.contactInfo = :contactInfo AND r.roleName = :roleName")
    Optional<AdminProfile> findByContactInfoAndAccountRole(@Param("contactInfo") String contactInfo, @Param("roleName") Role.RoleName roleName);

    boolean existsByContactInfo(String contactInfo);
}