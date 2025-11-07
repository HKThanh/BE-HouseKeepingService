package iuh.house_keeping_service_be.services.CustomerService.impl;

import iuh.house_keeping_service_be.dtos.Customer.CustomerUpdateRequest;
import iuh.house_keeping_service_be.dtos.Customer.response.CustomerProfileResponse;
import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.models.Customer;
import iuh.house_keeping_service_be.models.Role;
import iuh.house_keeping_service_be.repositories.AddressRepository;
import iuh.house_keeping_service_be.repositories.CustomerRepository;
import iuh.house_keeping_service_be.services.CustomerService.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AddressRepository addressRepository;

    @Override
    public Customer findByAccountId(String accountId) {
        return customerRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng: " + accountId));
    }

    @Override
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng: " + email));
    }

    @Override
    public Customer findByPhoneNumber(String phoneNumber) {
        return customerRepository.findByAccount_PhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng: " + phoneNumber));
    }

    @Override
    public Customer findById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));
    }

    @Override
    public CustomerProfileResponse getCustomerProfile(String customerId) {
        Customer customer = findById(customerId);
        
        // Get all addresses for this customer
        var addresses = addressRepository.findByCustomer_CustomerId(customerId).stream()
                .map(address -> CustomerProfileResponse.AddressInfo.builder()
                        .addressId(address.getAddressId())
                        .fullAddress(address.getFullAddress())
                        .ward(address.getWard())
                        .city(address.getCity())
                        .latitude(address.getLatitude())
                        .longitude(address.getLongitude())
                        .isDefault(address.getIsDefault())
                        .build())
                .collect(Collectors.toList());
        
        return CustomerProfileResponse.builder()
                .customerId(customer.getCustomerId())
                .fullName(customer.getFullName())
                .avatar(customer.getAvatar())
                .isMale(customer.getIsMale())
                .email(customer.getEmail())
                .birthdate(customer.getBirthdate())
                .rating(customer.getRating())
                .vipLevel(customer.getVipLevel())
                .account(CustomerProfileResponse.AccountInfo.builder()
                        .accountId(customer.getAccount().getAccountId())
                        .phoneNumber(customer.getAccount().getPhoneNumber())
                        .status(customer.getAccount().getStatus())
                        .isPhoneVerified(customer.getAccount().getIsPhoneVerified())
                        .lastLogin(customer.getAccount().getLastLogin())
                        .roles(customer.getAccount().getRoles().stream()
                                .map(Role::getRoleName)
                                .map(Enum::name)
                                .collect(Collectors.toList()))
                        .build())
                .addresses(addresses)
                .build();
    }

    @Override
    public Page<Customer> getActiveCustomers(Pageable pageable) {
        return customerRepository.findAllByAccount_Status(AccountStatus.ACTIVE, pageable);
    }

    @Override
    public Customer updateCustomer(String id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));

        if (request.fullName() != null) {
            customer.setFullName(request.fullName());
        }
        if (request.isMale() != null) {
            customer.setIsMale(request.isMale());
        }
        if (request.avatar() != null) {
            customer.setAvatar(request.avatar());
        }
        if (request.email() != null) {
            customer.setEmail(request.email());
        }
        if (request.birthdate() != null) {
            customer.setBirthdate(request.birthdate());
        }

        return customerRepository.save(customer);
    }

    @Override
    public Customer inActivateCustomer(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));
        customer.getAccount().setStatus(AccountStatus.INACTIVE);
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateAvatar(String id, String avatarUrl) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));
        customer.setAvatar(avatarUrl);
        return customerRepository.save(customer);
    }
}