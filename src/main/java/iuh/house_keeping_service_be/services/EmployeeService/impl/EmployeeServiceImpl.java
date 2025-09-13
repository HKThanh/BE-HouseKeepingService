package iuh.house_keeping_service_be.services.EmployeeService.impl;

import iuh.house_keeping_service_be.dtos.Employee.UpdateEmployeeRequest;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import iuh.house_keeping_service_be.services.EmployeeService.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Override
    public Employee updateEmployee(String employeeId, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin nhân viên"));

        if (request.getFullName() != null) {
            employee.setFullName(request.getFullName());
        }
        if (request.getIsMale() != null) {
            employee.setIsMale(request.getIsMale());
        }
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getBirthdate() != null) {
            employee.setBirthdate(request.getBirthdate());
        }
        if (request.getHiredDate() != null) {
            employee.setHiredDate(request.getHiredDate());
        }
        if (request.getSkills() != null) {
            employee.setSkills(request.getSkills());
        }
        if (request.getBio() != null) {
            employee.setBio(request.getBio());
        }
        if (request.getAvatar() != null) {
            employee.setAvatar(request.getAvatar());
        }
        if (request.getEmployeeStatus() != null) {
            employee.setEmployeeStatus(request.getEmployeeStatus());
        }
        if (request.getRating() != null) {
            employee.setRating(request.getRating());
        }
        if (request.getPhoneNumber() != null) {
            Account account = employee.getAccount();
            if (account != null && !request.getPhoneNumber().equals(account.getPhoneNumber())) {
                if (employeeRepository.existsByAccount_PhoneNumber(request.getPhoneNumber())) {
                    throw new IllegalArgumentException("Số điện thoại đã tồn tại");
                }
                account.setPhoneNumber(request.getPhoneNumber());
            }
        }

        return employeeRepository.save(employee);
    }

}