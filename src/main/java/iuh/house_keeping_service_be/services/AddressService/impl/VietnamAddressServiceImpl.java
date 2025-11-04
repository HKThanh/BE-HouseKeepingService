package iuh.house_keeping_service_be.services.AddressService.impl;

import iuh.house_keeping_service_be.dtos.Address.VietnamAddressDTO;
import iuh.house_keeping_service_be.dtos.Address.VietnamAddressResponseDTO;
import iuh.house_keeping_service_be.dtos.Address.VietnamProvinceDTO;
import iuh.house_keeping_service_be.dtos.Address.VietnamProvinceResponseDTO;
import iuh.house_keeping_service_be.services.AddressService.VietnamAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class VietnamAddressServiceImpl implements VietnamAddressService {
    
    private static final String VIETNAM_ADDRESS_API_URL = "https://production.cas.so/address-kit/latest/communes";
    private static final String PROVINCE_COMMUNES_API_URL = "https://production.cas.so/address-kit/{effectiveDate}/provinces/{provinceID}/communes";
    private static final String PROVINCES_API_URL = "https://production.cas.so/address-kit/{effectiveDate}/provinces";
    
    private final RestTemplate restTemplate;

    @Autowired
    public VietnamAddressServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = "vietnamAddressCache", unless = "#result == null || #result.isEmpty()")
    public List<VietnamAddressDTO> getVietnamAddressData() {
        try {
            log.info("Fetching Vietnam address data from external API: {}", VIETNAM_ADDRESS_API_URL);
            
            // Fetch and parse directly
            VietnamAddressResponseDTO responseDTO = restTemplate.getForObject(
                    VIETNAM_ADDRESS_API_URL,
                    VietnamAddressResponseDTO.class
            );
            
            List<VietnamAddressDTO> addressList = responseDTO != null && responseDTO.getCommunes() != null ? 
                    responseDTO.getCommunes() : Collections.emptyList();
            
            log.info("Successfully fetched Vietnam address data with {} communes", addressList.size());
            return addressList;
        } catch (Exception e) {
            log.error("Error fetching Vietnam address data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Vietnam address data", e);
        }
    }

    @Override
    @Cacheable(value = "provinceCommunesCache", key = "#effectiveDate + '_' + #provinceId", 
               unless = "#result == null || #result.isEmpty()")
    public List<VietnamAddressDTO> getCommunesByProvinceAndDate(String effectiveDate, String provinceId) {
        try {
            log.info("Fetching communes for province {} at date {}", provinceId, effectiveDate);
            
            // Build URL with path variables
            String url = PROVINCE_COMMUNES_API_URL
                    .replace("{effectiveDate}", effectiveDate)
                    .replace("{provinceID}", provinceId);
            
            // Fetch and parse directly
            VietnamAddressResponseDTO responseDTO = restTemplate.getForObject(
                    url,
                    VietnamAddressResponseDTO.class
            );
            
            List<VietnamAddressDTO> addressList = responseDTO != null && responseDTO.getCommunes() != null ? 
                    responseDTO.getCommunes() : Collections.emptyList();
            
            log.info("Successfully fetched {} communes for province {}", addressList.size(), provinceId);
            return addressList;
        } catch (Exception e) {
            log.error("Error fetching communes for province {} at date {}: {}", 
                    provinceId, effectiveDate, e.getMessage(), e);
            throw new RuntimeException(
                    String.format("Failed to fetch communes for province %s at date %s", provinceId, effectiveDate), 
                    e
            );
        }
    }

    @Override
    @Cacheable(value = "provincesCache", key = "#effectiveDate", 
               unless = "#result == null || #result.isEmpty()")
    public List<VietnamProvinceDTO> getProvincesByDate(String effectiveDate) {
        try {
            log.info("Fetching provinces at date {}", effectiveDate);
            
            // Build URL with path variable
            String url = PROVINCES_API_URL.replace("{effectiveDate}", effectiveDate);
            
            // Fetch and parse directly
            VietnamProvinceResponseDTO responseDTO = restTemplate.getForObject(
                    url,
                    VietnamProvinceResponseDTO.class
            );
            
            List<VietnamProvinceDTO> provinceList = responseDTO != null && responseDTO.getProvinces() != null ? 
                    responseDTO.getProvinces() : Collections.emptyList();
            
            log.info("Successfully fetched {} provinces", provinceList.size());
            return provinceList;
        } catch (Exception e) {
            log.error("Error fetching provinces at date {}: {}", 
                    effectiveDate, e.getMessage(), e);
            throw new RuntimeException(
                    String.format("Failed to fetch provinces at date %s", effectiveDate), 
                    e
            );
        }
    }
}
