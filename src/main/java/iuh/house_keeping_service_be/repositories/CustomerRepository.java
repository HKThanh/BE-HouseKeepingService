package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.models.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByAccount_AccountId(String accountId);

    Optional<Customer> findByAccount_Username(String username);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByAccount_PhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByAccount_PhoneNumber(String phoneNumber);

    Page<Customer> findAllByAccount_Status(AccountStatus status, Pageable pageable);

    @Query("SELECT c FROM Customer c " +
           "LEFT JOIN FETCH c.account a " +
           "WHERE (:status IS NULL OR a.status = :status)")
    Page<Customer> findAllWithAccountByStatus(@Param("status") AccountStatus status, Pageable pageable);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.account")
    Page<Customer> findAllWithAccount(Pageable pageable);
}