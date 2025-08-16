package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Customer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, String> {

    Optional<Customer> findCustomerByAccount_AccountId(String accountId);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);
}
