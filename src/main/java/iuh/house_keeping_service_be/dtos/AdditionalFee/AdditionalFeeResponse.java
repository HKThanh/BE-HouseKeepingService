package iuh.house_keeping_service_be.dtos.AdditionalFee;

import iuh.house_keeping_service_be.enums.AdditionalFeeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdditionalFeeResponse {
    private String id;
    private String name;
    private String description;
    private AdditionalFeeType feeType;
    private BigDecimal value;
    private boolean systemSurcharge;
    private boolean active;
    private int priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
