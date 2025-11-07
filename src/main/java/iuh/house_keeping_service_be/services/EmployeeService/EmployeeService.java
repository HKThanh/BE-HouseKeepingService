package iuh.house_keeping_service_be.services.EmployeeService;

import iuh.house_keeping_service_be.dtos.Employee.UpdateEmployeeRequest;
import iuh.house_keeping_service_be.dtos.Employee.response.EmployeeProfileResponse;
import iuh.house_keeping_service_be.models.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    Employee findByAccountId(String accountId);

    Employee findByEmail(String email);

    Employee findByPhoneNumber(String phoneNumber);

    Employee findById(String id);
    
    EmployeeProfileResponse getEmployeeProfile(String employeeId);

    Page<Employee> getAllEmployees(Pageable pageable);

    Employee updateEmployee(String employeeId, UpdateEmployeeRequest request);

    Employee updateAvatar(String employeeId, String avatarUrl);
}
