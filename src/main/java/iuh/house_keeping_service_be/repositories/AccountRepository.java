package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByPhoneNumber(String phoneNumber);

    List<Account> findAccountsByUsernameAndPassword(String username, String password);

    List<Account> findAccountsByUsername(String username);

    @Query("SELECT a FROM Account a JOIN a.roles r WHERE a.username = :username AND r.roleName = :roleName ORDER BY a.createdAt DESC")
    List<Account> findAccountsByUsernameAndRole(@Param("username") String username, @Param("roleName") RoleName roleName);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);
}