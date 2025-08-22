package iuh.house_keeping_service_be.services.AddressService.impl;

import iuh.house_keeping_service_be.models.Address;
import iuh.house_keeping_service_be.repositories.AddressRepository;
import iuh.house_keeping_service_be.services.AddressService.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Override
    public Address findByCustomerId(String customerId) {
        return addressRepository.findByCustomer_CustomerIdAndIsDefaultTrue(customerId)
                .orElseThrow(() -> new RuntimeException("No default address found for customer with ID: " + customerId));
    }
}
