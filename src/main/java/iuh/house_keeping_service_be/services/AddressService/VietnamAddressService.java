package iuh.house_keeping_service_be.services.AddressService;

import iuh.house_keeping_service_be.dtos.Address.VietnamAddressDTO;
import iuh.house_keeping_service_be.dtos.Address.VietnamProvinceDTO;

import java.util.List;

public interface VietnamAddressService {
    /**
     * Fetch Vietnam address data from external API
     * @return List of VietnamAddressDTO containing communes/wards data
     */
    List<VietnamAddressDTO> getVietnamAddressData();
    
    /**
     * Fetch communes by province ID and effective date
     * @param effectiveDate Effective date in format yyyy-MM-dd
     * @param provinceId Province ID
     * @return List of VietnamAddressDTO containing communes/wards data for the province
     */
    List<VietnamAddressDTO> getCommunesByProvinceAndDate(String effectiveDate, String provinceId);
    
    /**
     * Fetch provinces by effective date
     * @param effectiveDate Effective date in format yyyy-MM-dd
     * @return List of VietnamProvinceDTO containing provinces data
     */
    List<VietnamProvinceDTO> getProvincesByDate(String effectiveDate);
}
