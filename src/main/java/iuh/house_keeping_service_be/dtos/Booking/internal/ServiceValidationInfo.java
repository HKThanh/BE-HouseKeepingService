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
public class ServiceValidationInfo {
    private Integer serviceId;
    private String serviceName;
    private boolean exists;
    private boolean active;
    private BigDecimal basePrice;
    private List<Integer> validChoiceIds;
    private List<Integer> invalidChoiceIds;
    private BigDecimal calculatedPrice;
    private BigDecimal expectedPrice;
    private boolean priceMatches;
    private String errorMessage;
    
    public static ServiceValidationInfo invalid(Integer serviceId, String errorMessage) {
        return ServiceValidationInfo.builder()
            .serviceId(serviceId)
            .serviceName("Unknown Service")
            .exists(false)
            .active(false)
            .priceMatches(false)
            .errorMessage(errorMessage)
            .calculatedPrice(BigDecimal.ZERO)
            .expectedPrice(BigDecimal.ZERO)
            .validChoiceIds(List.of())
            .invalidChoiceIds(List.of())
            .build();
    }
    
    public boolean isValid() {
        return exists && active && priceMatches && (invalidChoiceIds == null || invalidChoiceIds.isEmpty());
    }
}