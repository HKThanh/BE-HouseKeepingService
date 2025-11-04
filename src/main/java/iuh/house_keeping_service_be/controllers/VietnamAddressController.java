package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Address.VietnamAddressDTO;
import iuh.house_keeping_service_be.dtos.Address.VietnamProvinceDTO;
import iuh.house_keeping_service_be.services.AddressService.VietnamAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@Slf4j
public class VietnamAddressController {

    @Autowired
    private VietnamAddressService vietnamAddressService;

    /**
     * Get Vietnam address data including all communes/wards
     * This endpoint is publicly accessible without authentication
     * 
     * @return ResponseEntity containing list of Vietnam address data
     */
    @GetMapping("/vietnam")
    public ResponseEntity<?> getVietnamAddresses() {
        try {
            log.info("Received request to get Vietnam address data");
            List<VietnamAddressDTO> addressData = vietnamAddressService.getVietnamAddressData();
            
            if (addressData == null || addressData.isEmpty()) {
                log.warn("No address data available");
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .body("No address data available");
            }
            
            log.info("Successfully retrieved Vietnam address data with {} communes", 
                    addressData.size());
            return ResponseEntity.ok(addressData);
            
        } catch (Exception e) {
            log.error("Error retrieving Vietnam address data: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving address data: " + e.getMessage());
        }
    }

    /**
     * Get communes by province ID and effective date
     * This endpoint is publicly accessible without authentication
     * 
     * @param effectiveDate Effective date in format yyyy-MM-dd (e.g., 2025-06-16)
     * @param provinceId Province ID (e.g., 01 for Hà Nội, 79 for TP.HCM)
     * @return ResponseEntity containing list of communes for the province
     */
    @GetMapping("/{effectiveDate}/provinces/{provinceId}/communes")
    public ResponseEntity<?> getCommunesByProvinceAndDate(
            @PathVariable String effectiveDate,
            @PathVariable String provinceId) {
        try {
            log.info("Received request to get communes for province {} at date {}", 
                    provinceId, effectiveDate);
            
            List<VietnamAddressDTO> communes = vietnamAddressService
                    .getCommunesByProvinceAndDate(effectiveDate, provinceId);
            
            if (communes == null || communes.isEmpty()) {
                log.warn("No communes found for province {} at date {}", provinceId, effectiveDate);
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .body("No communes found for the specified province and date");
            }
            
            log.info("Successfully retrieved {} communes for province {}", 
                    communes.size(), provinceId);
            return ResponseEntity.ok(communes);
            
        } catch (Exception e) {
            log.error("Error retrieving communes for province {} at date {}: {}", 
                    provinceId, effectiveDate, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving communes: " + e.getMessage());
        }
    }

    /**
     * Get provinces by effective date
     * This endpoint is publicly accessible without authentication
     * 
     * @param effectiveDate Effective date in format yyyy-MM-dd (e.g., 2025-06-16)
     * @return ResponseEntity containing list of provinces
     */
    @GetMapping("/{effectiveDate}/provinces")
    public ResponseEntity<?> getProvincesByDate(@PathVariable String effectiveDate) {
        try {
            log.info("Received request to get provinces at date {}", effectiveDate);
            
            List<VietnamProvinceDTO> provinces = vietnamAddressService.getProvincesByDate(effectiveDate);
            
            if (provinces == null || provinces.isEmpty()) {
                log.warn("No provinces found for date {}", effectiveDate);
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .body("No provinces found for the specified date");
            }
            
            log.info("Successfully retrieved {} provinces", provinces.size());
            return ResponseEntity.ok(provinces);
            
        } catch (Exception e) {
            log.error("Error retrieving provinces at date {}: {}", 
                    effectiveDate, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving provinces: " + e.getMessage());
        }
    }
}
