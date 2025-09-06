package iuh.house_keeping_service_be.dtos.Booking.response;

import iuh.house_keeping_service_be.dtos.Booking.internal.ConflictInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingErrorResponse {
    private boolean success = false;
    private String message;
    private String errorCode;
    private List<String> validationErrors;
    private List<ConflictInfo> conflicts;
    
    public static BookingErrorResponse validationError(String message, List<String> errors) {
        return new BookingErrorResponse(false, message, "VALIDATION_ERROR", errors, null);
    }
    
    public static BookingErrorResponse conflictError(String message, List<ConflictInfo> conflicts) {
        return new BookingErrorResponse(false, message, "CONFLICT_ERROR", null, conflicts);
    }
    
    public static BookingErrorResponse businessError(String message, String errorCode) {
        return new BookingErrorResponse(false, message, errorCode, null, null);
    }
}