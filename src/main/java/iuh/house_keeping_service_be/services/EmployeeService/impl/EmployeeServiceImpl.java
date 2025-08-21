package iuh.house_keeping_service_be.services.EmployeeService.impl;

import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.services.EmployeeService.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee findByAccountId(String accountId) {
        return employeeRepository.findEmployeeByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Employee not found with account ID: " + accountId));
    }

    @Override
    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
    }

    @Override
    public Employee findByPhoneNumber(String phoneNumber) {
        return employeeRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Employee not found with phone number: " + phoneNumber));
    }

    @Override
    public Employee findById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));
    }
}
