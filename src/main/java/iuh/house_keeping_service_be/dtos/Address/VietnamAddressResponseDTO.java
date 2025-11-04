package iuh.house_keeping_service_be.dtos.Address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO for Vietnam address API response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VietnamAddressResponseDTO {
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("communes")
    private List<VietnamAddressDTO> communes;
}
