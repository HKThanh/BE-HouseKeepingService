package iuh.house_keeping_service_be.services.CustomerService;

import iuh.house_keeping_service_be.dtos.Customer.CustomerUpdateRequest;
import iuh.house_keeping_service_be.models.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Customer findByAccountId(String accountId);

    Customer findByEmail(String email);

    Customer findByPhoneNumber(String phoneNumber);

    Customer findById(String id);

    Page<Customer> getActiveCustomers(Pageable pageable);

    Customer updateCustomer(String id, CustomerUpdateRequest request);

    Customer inActivateCustomer(String id);
}
