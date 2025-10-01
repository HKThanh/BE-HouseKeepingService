package iuh.house_keeping_service_be.exceptions;

public class ReviewCriteriaNotFoundException extends ResourceNotFoundException {
    public ReviewCriteriaNotFoundException(String message) {
        super(message);
    }
}