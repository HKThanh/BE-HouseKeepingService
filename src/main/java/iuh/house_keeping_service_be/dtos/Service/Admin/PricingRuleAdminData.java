package iuh.house_keeping_service_be.dtos.Service.Admin;

import iuh.house_keeping_service_be.enums.ConditionLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingRuleAdminData {
    private Integer ruleId;
    private Integer serviceId;
    private String serviceName;
    private String ruleName;
    private ConditionLogic conditionLogic;
    private Integer priority;
    private Boolean isActive;
    private BigDecimal priceAdjustment;
    private Integer staffAdjustment;
    private BigDecimal durationAdjustmentHours;
}
