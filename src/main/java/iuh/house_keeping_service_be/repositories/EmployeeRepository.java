package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Employee;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<Employee, String> {
    Optional<Employee> findEmployeeByAccount_AccountId(String accountId);
}
