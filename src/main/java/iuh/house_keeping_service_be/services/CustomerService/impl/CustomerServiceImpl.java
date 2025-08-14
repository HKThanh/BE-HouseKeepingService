package iuh.house_keeping_service_be.services.CustomerService.impl;

import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.services.CustomerService.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer findByAccountId(String accountId) {
        return customerRepository.findCustomerByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Customer not found with account ID: " + accountId));
    }
}
