package iuh.house_keeping_service_be.dtos.Address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VietnamProvinceDTO {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("englishName")
    private String englishName;
    
    @JsonProperty("administrativeLevel")
    private String administrativeLevel;
    
    @JsonProperty("decree")
    private String decree;
}
