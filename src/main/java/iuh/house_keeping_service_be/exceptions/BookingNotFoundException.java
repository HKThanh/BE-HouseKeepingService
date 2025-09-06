package iuh.house_keeping_service_be.exceptions;

public class BookingNotFoundException extends ResourceNotFoundException {
    
    public BookingNotFoundException(String message) {
        super(message);
    }
    
    public BookingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Static factory methods cho clarity
    public static BookingNotFoundException withId(String bookingId) {
        return new BookingNotFoundException("Booking not found with ID: " + bookingId);
    }
    
    public static BookingNotFoundException withCode(String bookingCode) {
        return new BookingNotFoundException("Booking not found with code: " + bookingCode);
    }
    
    public static BookingNotFoundException withCustomMessage(String message) {
        return new BookingNotFoundException(message);
    }
}