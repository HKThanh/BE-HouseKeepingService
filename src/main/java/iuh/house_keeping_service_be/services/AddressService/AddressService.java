package iuh.house_keeping_service_be.services.AddressService;

import iuh.house_keeping_service_be.models.Address;

public interface AddressService {
    Address findByCustomerId(String customerId);
}
