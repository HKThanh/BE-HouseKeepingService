package iuh.house_keeping_service_be.services.EmployeeService.impl;

import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.services.EmployeeService.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee findByAccountId(String accountId) {
        return employeeRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên: " + accountId));
    }

    @Override
    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên: " + email));
    }

    @Override
    public Employee findByPhoneNumber(String phoneNumber) {
        return employeeRepository.findByAccount_PhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên: " + phoneNumber));
    }

    @Override
    public Employee findById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));
    }
}