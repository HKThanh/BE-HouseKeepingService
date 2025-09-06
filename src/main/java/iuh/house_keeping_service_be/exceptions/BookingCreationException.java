package iuh.house_keeping_service_be.exceptions;

public class BookingCreationException extends RuntimeException {
    
    public BookingCreationException(String message) {
        super(message);
    }
    
    public BookingCreationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Static factory methods
    public static BookingCreationException withCause(String message, Throwable cause) {
        return new BookingCreationException("Failed to create booking: " + message, cause);
    }
    
    public static BookingCreationException databaseError(Throwable cause) {
        return new BookingCreationException("Database error during booking creation", cause);
    }
    
    public static BookingCreationException businessRuleViolation(String rule) {
        return new BookingCreationException("Business rule violation: " + rule);
    }
    
    public static BookingCreationException timeout() {
        return new BookingCreationException("Booking creation timed out - please try again");
    }
}