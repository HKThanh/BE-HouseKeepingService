package iuh.house_keeping_service_be.exceptions;

public class ReviewBookingStateException extends RuntimeException {
    public ReviewBookingStateException(String message) {
        super(message);
    }
}