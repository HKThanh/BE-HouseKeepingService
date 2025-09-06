package iuh.house_keeping_service_be.dtos.Booking.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}