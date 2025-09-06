package iuh.house_keeping_service_be.dtos.Booking.response;

import iuh.house_keeping_service_be.dtos.Booking.internal.ConflictInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private List<String> validationErrors;
    private List<ConflictInfo> conflicts;
    private LocalDateTime timestamp;
    
    public static BookingErrorResponse validationError(String message, List<String> errors) {
        return BookingErrorResponse.builder()
            .success(false)
            .message(message)
            .errorCode("VALIDATION_ERROR")
            .validationErrors(errors)
            .conflicts(List.of())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static BookingErrorResponse conflictError(String message, List<ConflictInfo> conflicts) {
        return BookingErrorResponse.builder()
            .success(false)
            .message(message)
            .errorCode("CONFLICT_ERROR")
            .validationErrors(List.of())
            .conflicts(conflicts)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static BookingErrorResponse businessError(String message, String errorCode) {
        return BookingErrorResponse.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .validationErrors(List.of())
            .conflicts(List.of())
            .timestamp(LocalDateTime.now())
            .build();
    }
}