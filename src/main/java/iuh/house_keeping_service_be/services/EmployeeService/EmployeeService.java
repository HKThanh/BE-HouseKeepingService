package iuh.house_keeping_service_be.services.EmployeeService;

import iuh.house_keeping_service_be.models.Employee;

public interface EmployeeService {
    Employee findByAccountId(String accountId);
}
