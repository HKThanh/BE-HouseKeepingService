package iuh.house_keeping_service_be.dtos.Service.Admin;

import iuh.house_keeping_service_be.enums.ConditionLogic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePricingRuleRequest {
    
    @NotNull(message = "Service ID không được để trống")
    private Integer serviceId;
    
    @NotBlank(message = "Tên rule không được để trống")
    @Size(max = 255, message = "Tên rule không được vượt quá 255 ký tự")
    private String ruleName;
    
    private ConditionLogic conditionLogic;
    
    private Integer priority = 0;
    
    private BigDecimal priceAdjustment = BigDecimal.ZERO;
    
    private Integer staffAdjustment = 0;
    
    private BigDecimal durationAdjustmentHours = BigDecimal.ZERO;
}
