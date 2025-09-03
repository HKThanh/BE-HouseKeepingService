package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Service.ServiceListResponse;
import iuh.house_keeping_service_be.services.ServiceService.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<ServiceListResponse> getAllServices() {
        try {
            log.info("Getting all active services - public endpoint");

            ServiceListResponse response = serviceService.getAllActiveServices();

            if (response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            log.error("Error getting all services: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                new ServiceListResponse(false, "Lỗi hệ thống khi lấy danh sách dịch vụ", null)
            );
        }
    }
}