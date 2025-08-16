package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Employee;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<Employee, String> {
    Optional<Employee> findEmployeeByAccount_AccountId(String accountId);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);
}
