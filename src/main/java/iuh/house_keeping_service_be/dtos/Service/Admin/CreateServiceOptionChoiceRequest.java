package iuh.house_keeping_service_be.dtos.Service.Admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceOptionChoiceRequest {
    
    @NotNull(message = "Option ID không được để trống")
    private Integer optionId;
    
    @NotBlank(message = "Label không được để trống")
    @Size(max = 255, message = "Label không được vượt quá 255 ký tự")
    private String label;
    
    private Boolean isDefault = false;
    
    private Integer displayOrder;
}
