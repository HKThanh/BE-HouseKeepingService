package iuh.house_keeping_service_be.exceptions;

import java.util.List;

public class BookingValidationException extends RuntimeException {
    private final List<String> errors;
    
    public BookingValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}