package iuh.house_keeping_service_be.exceptions;

public class AddressNotFoundException extends ResourceNotFoundException {
    
    public AddressNotFoundException(String message) {
        super(message);
    }
    
    public AddressNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Static factory methods
    public static AddressNotFoundException withId(String addressId) {
        return new AddressNotFoundException("Address not found with ID: " + addressId);
    }
    
    public static AddressNotFoundException forCustomer(String customerId) {
        return new AddressNotFoundException("No address found for customer ID: " + customerId);
    }
    
    public static AddressNotFoundException defaultAddress(String customerId) {
        return new AddressNotFoundException("No default address found for customer ID: " + customerId);
    }
    
    public static AddressNotFoundException withCustomMessage(String message) {
        return new AddressNotFoundException(message);
    }
}