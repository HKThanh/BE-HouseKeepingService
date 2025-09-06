package iuh.house_keeping_service_be.dtos.Booking.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceValidationResult {
    private boolean valid;
    private BigDecimal calculatedPrice;
    private BigDecimal expectedPrice;
    private BigDecimal difference;
    private List<String> appliedRules;
    private String errorMessage;
    
    public static PriceValidationResult success(BigDecimal calculatedPrice, BigDecimal expectedPrice, List<String> appliedRules) {
        return new PriceValidationResult(true, calculatedPrice, expectedPrice, 
                calculatedPrice.subtract(expectedPrice), appliedRules, null);
    }
    
    public static PriceValidationResult error(BigDecimal calculatedPrice, BigDecimal expectedPrice, String errorMessage) {
        return new PriceValidationResult(false, calculatedPrice, expectedPrice, 
                calculatedPrice.subtract(expectedPrice), null, errorMessage);
    }
}