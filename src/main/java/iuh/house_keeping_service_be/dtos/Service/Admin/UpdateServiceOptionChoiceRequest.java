package iuh.house_keeping_service_be.dtos.Service.Admin;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceOptionChoiceRequest {
    
    @Size(max = 255, message = "Label không được vượt quá 255 ký tự")
    private String label;
    
    private Boolean isDefault;
    
    private Boolean isActive;
    
    private Integer displayOrder;
}
