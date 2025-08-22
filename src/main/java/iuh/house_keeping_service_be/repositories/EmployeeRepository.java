package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByAccount_AccountId(String accountId);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByAccount_PhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByAccount_PhoneNumber(String phoneNumber);
}