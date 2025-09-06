package iuh.house_keeping_service_be.dtos.Booking.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingValidationResult {
    private boolean valid;
    private List<String> errors;
    private List<ConflictInfo> conflicts;
    private BigDecimal calculatedTotalAmount;
    private List<ServiceValidationInfo> serviceValidations;
    
    public static BookingValidationResult success(BigDecimal totalAmount, List<ServiceValidationInfo> validations) {
        return BookingValidationResult.builder()
            .valid(true)
            .errors(List.of())
            .conflicts(List.of())
            .calculatedTotalAmount(totalAmount)
            .serviceValidations(validations)
            .build();
    }
    
    public static BookingValidationResult error(List<String> errors) {
        return BookingValidationResult.builder()
            .valid(false)
            .errors(errors)
            .conflicts(List.of())
            .build();
    }
    
    public static BookingValidationResult conflict(List<ConflictInfo> conflicts) {
        return BookingValidationResult.builder()
            .valid(false)
            .errors(List.of())
            .conflicts(conflicts)
            .build();
    }
}