package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends CrudRepository<Account, UUID> {
    Optional<Account> findByUsername(String username);
}
