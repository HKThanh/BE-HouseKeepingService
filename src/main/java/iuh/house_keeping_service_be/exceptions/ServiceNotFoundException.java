package iuh.house_keeping_service_be.exceptions;

public class ServiceNotFoundException extends ResourceNotFoundException {
    
    public ServiceNotFoundException(String message) {
        super(message);
    }
    
    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Static factory methods
    public static ServiceNotFoundException withId(Integer serviceId) {
        return new ServiceNotFoundException("Service not found with ID: " + serviceId);
    }
    
    public static ServiceNotFoundException notBookable(Integer serviceId) {
        return new ServiceNotFoundException("Service with ID " + serviceId + " is not available for booking");
    }
    
    public static ServiceNotFoundException inCategory(Integer categoryId) {
        return new ServiceNotFoundException("No active services found in category ID: " + categoryId);
    }
    
    public static ServiceNotFoundException withCustomMessage(String message) {
        return new ServiceNotFoundException(message);
    }
}