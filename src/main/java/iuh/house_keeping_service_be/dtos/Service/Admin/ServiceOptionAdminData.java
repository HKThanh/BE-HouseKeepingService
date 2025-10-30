package iuh.house_keeping_service_be.dtos.Service.Admin;

import iuh.house_keeping_service_be.enums.OptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOptionAdminData {
    private Integer optionId;
    private Integer serviceId;
    private String serviceName;
    private String label;
    private OptionType optionType;
    private Integer displayOrder;
    private Boolean isRequired;
    private Boolean isActive;
    private Integer parentOptionId;
    private Integer parentChoiceId;
    private Map<String, Object> validationRules;
    private List<ServiceOptionChoiceAdminData> choices;
}
