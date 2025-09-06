package iuh.house_keeping_service_be.exceptions;

import java.util.List;

public class BookingValidationException extends RuntimeException {
    private final List<String> errors;
    
    public BookingValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? List.copyOf(errors) : List.of();
    }
    
    public BookingValidationException(List<String> errors) {
        super("Booking validation failed");
        this.errors = errors != null ? List.copyOf(errors) : List.of();
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    // Static factory methods
    public static BookingValidationException withErrors(List<String> errors) {
        return new BookingValidationException("Booking validation failed", errors);
    }
    
    public static BookingValidationException singleError(String error) {
        return new BookingValidationException("Booking validation failed", List.of(error));
    }
}