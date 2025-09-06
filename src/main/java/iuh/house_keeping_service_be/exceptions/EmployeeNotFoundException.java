package iuh.house_keeping_service_be.exceptions;

public class EmployeeNotFoundException extends ResourceNotFoundException {
    
    public EmployeeNotFoundException(String message) {
        super(message);
    }
    
    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Static factory methods
    public static EmployeeNotFoundException withId(String employeeId) {
        return new EmployeeNotFoundException("Employee not found with ID: " + employeeId);
    }
    
    public static EmployeeNotFoundException withEmail(String email) {
        return new EmployeeNotFoundException("Employee not found with email: " + email);
    }
    
    public static EmployeeNotFoundException withPhone(String phoneNumber) {
        return new EmployeeNotFoundException("Employee not found with phone: " + phoneNumber);
    }
    
    public static EmployeeNotFoundException inArea(String district, String city) {
        return new EmployeeNotFoundException("No available employees found in " + district + ", " + city);
    }
    
    public static EmployeeNotFoundException withCustomMessage(String message) {
        return new EmployeeNotFoundException(message);
    }
}