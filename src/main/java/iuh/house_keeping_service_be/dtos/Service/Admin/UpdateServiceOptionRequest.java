package iuh.house_keeping_service_be.dtos.Service.Admin;

import iuh.house_keeping_service_be.enums.OptionType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceOptionRequest {
    
    @Size(max = 255, message = "Label không được vượt quá 255 ký tự")
    private String label;
    
    private OptionType optionType;
    
    private Integer displayOrder;
    
    private Boolean isRequired;
    
    private Boolean isActive;
    
    private Integer parentOptionId;
    
    private Integer parentChoiceId;
    
    private Map<String, Object> validationRules;
}
