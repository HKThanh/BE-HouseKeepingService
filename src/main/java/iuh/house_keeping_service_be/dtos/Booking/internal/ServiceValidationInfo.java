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
    
    public static ServiceValidationInfo invalid(Integer serviceId, String reason) {
        return ServiceValidationInfo.builder()
            .serviceId(serviceId)
            .serviceName(reason)
            .exists(false)
            .active(false)
            .priceMatches(false)
            .build();
    }
}