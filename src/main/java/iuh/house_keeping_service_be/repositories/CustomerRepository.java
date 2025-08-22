package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByAccount_AccountId(String accountId);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByAccount_PhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByAccount_PhoneNumber(String phoneNumber);
}