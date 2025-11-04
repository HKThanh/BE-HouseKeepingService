package iuh.house_keeping_service_be.dtos.Address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VietnamProvinceResponseDTO {
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("provinces")
    private List<VietnamProvinceDTO> provinces;
}
