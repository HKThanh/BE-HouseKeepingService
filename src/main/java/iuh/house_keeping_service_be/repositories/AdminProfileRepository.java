package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.AdminProfile;
import jakarta.validation.constraints.Size;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdminProfileRepository extends CrudRepository<AdminProfile, String> {
    Optional<AdminProfile> findByAccount_AccountId(String accountId);

    Optional<AdminProfile> findByContactInfoAndAccount_Role(@Size(max = 255) String contactInfo, Role account_role);
}
