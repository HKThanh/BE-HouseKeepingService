package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Account;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);

    List<Account> findAccountsByUsernameAndPassword(String username, String password);

    List<Account> findAccountsByUsername(String username);

//    Optional<Account> findAccountByUsernameAndRole(String username, Role role);

    @Query("SELECT a FROM Account a WHERE a.username = :username AND a.role = :role ORDER BY a.createdAt DESC")
    List<Account> findAccountsByUsernameAndRole(@Param("username") String username, @Param("role") Role role);
}
