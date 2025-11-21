package iuh.house_keeping_service_be.dtos.Booking.internal;

import iuh.house_keeping_service_be.enums.AdditionalFeeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeBreakdownInfo {
    private String name;
    private AdditionalFeeType type;
    private BigDecimal value;
    private BigDecimal amount;
    private boolean systemSurcharge;
}
