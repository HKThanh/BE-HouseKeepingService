package iuh.house_keeping_service_be.exceptions;

import java.math.BigDecimal;

public class PriceCalculationException extends RuntimeException {
    
    public record PriceContext(
        Integer serviceId, 
        String serviceName,
        BigDecimal expectedPrice, 
        BigDecimal calculatedPrice,
        String details
    ) {}
    
    private final PriceContext context;
    
    public PriceCalculationException(String message) {
        super(message);
        this.context = null;
    }
    
    public PriceCalculationException(String message, Throwable cause) {
        super(message, cause);
        this.context = null;
    }
    
    public PriceCalculationException(String message, PriceContext context) {
        super(message);
        this.context = context;
    }
    
    public PriceContext getContext() {
        return context;
    }
    
    // Static factory methods
    public static PriceCalculationException mismatch(Integer serviceId, String serviceName, 
                                                    BigDecimal expected, BigDecimal calculated) {
        var context = new PriceContext(serviceId, serviceName, expected, calculated, "Price mismatch");
        return new PriceCalculationException(
            "Price mismatch for service '%s' (ID: %d): expected %s, calculated %s"
                .formatted(serviceName, serviceId, expected, calculated),
            context
        );
    }
    
    public static PriceCalculationException invalidChoice(Integer serviceId, String serviceName, Integer choiceId) {
        var context = new PriceContext(serviceId, serviceName, null, null, "Invalid choice: " + choiceId);
        return new PriceCalculationException(
            "Invalid choice %d for service '%s' (ID: %d)".formatted(choiceId, serviceName, serviceId),
            context
        );
    }
    
    public static PriceCalculationException calculationError(Integer serviceId, String serviceName, Throwable cause) {
        var context = new PriceContext(serviceId, serviceName, null, null, "Calculation error");
        return new PriceCalculationException(
            "Failed to calculate price for service '%s' (ID: %d)".formatted(serviceName, serviceId),
            context
        );
    }
}