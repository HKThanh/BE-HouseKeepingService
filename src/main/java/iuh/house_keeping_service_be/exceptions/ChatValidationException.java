package iuh.house_keeping_service_be.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ChatValidationException extends RuntimeException {
    public ChatValidationException(String message) {
        super(message);
    }
}