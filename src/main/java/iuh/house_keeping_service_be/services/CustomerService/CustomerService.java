package iuh.house_keeping_service_be.services.CustomerService;

import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Customer;

public interface CustomerService {
    Customer findByAccountId(String accountId);

    Customer findByEmail(String email);

    Customer findByPhoneNumber(String phoneNumber);

    Customer findById(String id);
}
