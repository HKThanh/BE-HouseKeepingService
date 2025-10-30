package iuh.house_keeping_service_be.dtos.Service.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOptionChoiceAdminData {
    private Integer choiceId;
    private Integer optionId;
    private String label;
    private Boolean isDefault;
    private Boolean isActive;
    private Integer displayOrder;
}
