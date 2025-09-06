package iuh.house_keeping_service_be.exceptions;

public class CustomerNotFoundException extends ResourceNotFoundException {
    
    public CustomerNotFoundException(String message) {
        super(message);
    }
    
    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Static factory methods
    public static CustomerNotFoundException withId(String customerId) {
        return new CustomerNotFoundException("Customer not found with ID: " + customerId);
    }
    
    public static CustomerNotFoundException withEmail(String email) {
        return new CustomerNotFoundException("Customer not found with email: " + email);
    }
    
    public static CustomerNotFoundException withPhone(String phoneNumber) {
        return new CustomerNotFoundException("Customer not found with phone: " + phoneNumber);
    }
    
    public static CustomerNotFoundException forAddress(String addressId) {
        return new CustomerNotFoundException("No customer found for address ID: " + addressId);
    }
    
    public static CustomerNotFoundException withCustomMessage(String message) {
        return new CustomerNotFoundException(message);
    }
}