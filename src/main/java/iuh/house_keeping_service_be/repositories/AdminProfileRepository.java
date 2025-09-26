package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, String> {
    Optional<AdminProfile> findByAccount_AccountId(String accountId);

    Optional<AdminProfile> findByAccount_Username(String username);

    @Query("SELECT ap FROM AdminProfile ap JOIN ap.account.roles r WHERE ap.contactInfo = :contactInfo AND r.roleName = :roleName")
    Optional<AdminProfile> findByContactInfoAndAccountRole(@Param("contactInfo") String contactInfo, @Param("roleName") RoleName roleName);

    boolean existsByContactInfo(String contactInfo);

    boolean existsByAccount_Username(String username);
}