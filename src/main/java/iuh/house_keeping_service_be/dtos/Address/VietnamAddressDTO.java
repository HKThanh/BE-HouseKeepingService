package iuh.house_keeping_service_be.dtos.Address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a commune/ward in Vietnam
 * Maps to the response from https://production.cas.so/address-kit/latest/communes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VietnamAddressDTO {
    
    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("englishName")
    private String englishName;

    @JsonProperty("administrativeLevel")
    private String administrativeLevel;

    @JsonProperty("provinceCode")
    private String provinceCode;

    @JsonProperty("provinceName")
    private String provinceName;

    @JsonProperty("decree")
    private String decree;
}
