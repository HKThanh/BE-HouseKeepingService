package iuh.house_keeping_service_be.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ChatMessageOperationException extends RuntimeException {
    public ChatMessageOperationException(String message) {
        super(message);
    }
}