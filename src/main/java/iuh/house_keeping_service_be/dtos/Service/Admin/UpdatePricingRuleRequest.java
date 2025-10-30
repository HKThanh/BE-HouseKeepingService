package iuh.house_keeping_service_be.dtos.Service.Admin;

import iuh.house_keeping_service_be.enums.ConditionLogic;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePricingRuleRequest {
    
    @Size(max = 255, message = "Tên rule không được vượt quá 255 ký tự")
    private String ruleName;
    
    private ConditionLogic conditionLogic;
    
    private Integer priority;
    
    private Boolean isActive;
    
    private BigDecimal priceAdjustment;
    
    private Integer staffAdjustment;
    
    private BigDecimal durationAdjustmentHours;
}
