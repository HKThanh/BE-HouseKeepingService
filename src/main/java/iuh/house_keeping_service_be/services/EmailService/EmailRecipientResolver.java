package iuh.house_keeping_service_be.services.EmailService;

import iuh.house_keeping_service_be.models.AdminProfile;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Employee;
import iuh.house_keeping_service_be.repositories.AdminProfileRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRecipientResolver {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final AdminProfileRepository adminProfileRepository;

    public Optional<String> resolveEmailByAccountId(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return Optional.empty();
        }

        return tryCustomerEmail(accountId)
                .or(() -> tryEmployeeEmail(accountId))
                .or(() -> tryAdminEmail(accountId));
    }

    private Optional<String> tryCustomerEmail(String accountId) {
        return customerRepository.findByAccount_AccountId(accountId)
                .map(Customer::getEmail)
                .filter(this::isValidEmail);
    }

    private Optional<String> tryEmployeeEmail(String accountId) {
        return employeeRepository.findByAccount_AccountId(accountId)
                .map(Employee::getEmail)
                .filter(this::isValidEmail);
    }

    private Optional<String> tryAdminEmail(String accountId) {
        return adminProfileRepository.findByAccount_AccountId(accountId)
                .map(AdminProfile::getContactInfo)
                .filter(this::isValidEmail);
    }

    private boolean isValidEmail(String email) {
        boolean valid = StringUtils.hasText(email) && email.contains("@");
        if (!valid) {
            log.debug("Filtered invalid email: {}", email);
        }
        return valid;
    }
}
