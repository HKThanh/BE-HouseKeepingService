package iuh.house_keeping_service_be.dtos.Service.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAdminData {
    private Integer serviceId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String unit;
    private BigDecimal estimatedDurationHours;
    private Integer recommendedStaff;
    private String iconUrl;
    private Boolean isActive;
    private Integer categoryId;
    private String categoryName;
    private Integer optionsCount;
    private Integer pricingRulesCount;
}
