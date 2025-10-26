package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.AccountRole;
import iuh.house_keeping_service_be.models.AccountRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole, AccountRoleId> {

    @Query("SELECT ar FROM AccountRole ar WHERE ar.account.accountId = :accountId")
    List<AccountRole> findByAccountId(@Param("accountId") String accountId);

}