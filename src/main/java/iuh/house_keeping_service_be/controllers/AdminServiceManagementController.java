package iuh.house_keeping_service_be.controllers;

import iuh.house_keeping_service_be.dtos.Service.Admin.*;
import iuh.house_keeping_service_be.services.AdminServiceManagement.AdminServiceManagementService;
import iuh.house_keeping_service_be.services.MediaService.CloudinaryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/services")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceManagementController {

    @Autowired
    private AdminServiceManagementService adminServiceManagementService;

    @Autowired
    private CloudinaryService cloudinaryService;

    // ===================== SERVICE ENDPOINTS =====================

    /**
     * Get all services with pagination
     * GET /api/v1/admin/services?page=0&size=10&sortBy=name&sortDir=asc
     */
    @GetMapping
    public ResponseEntity<?> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Page<ServiceAdminData> services = adminServiceManagementService.getAllServices(page, size, sortBy, sortDir);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", services.getContent(),
                    "currentPage", services.getNumber(),
                    "totalItems", services.getTotalElements(),
                    "totalPages", services.getTotalPages()
            ));
        } catch (Exception e) {
            log.error("Error fetching services: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy danh sách dịch vụ"
            ));
        }
    }

    /**
     * Get service by ID
     * GET /api/v1/admin/services/{serviceId}
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<?> getServiceById(@PathVariable Integer serviceId) {
        try {
            ServiceAdminData service = adminServiceManagementService.getServiceById(serviceId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", service
            ));
        } catch (IllegalArgumentException e) {
            log.error("Service not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy thông tin dịch vụ"
            ));
        }
    }

    /**
     * Create new service with icon upload
     * POST /api/v1/admin/services
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createServiceWithIcon(
            @RequestParam("name") String name,
            @RequestParam("basePrice") String basePrice,
            @RequestParam("unit") String unit,
            @RequestParam("recommendedStaff") String recommendedStaff,
            @RequestParam("categoryId") String categoryId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "estimatedDurationHours", required = false) String estimatedDurationHours,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        try {
            CreateServiceRequest request = new CreateServiceRequest();
            request.setName(name);
            request.setDescription(description);
            request.setBasePrice(new java.math.BigDecimal(basePrice));
            request.setUnit(unit);
            request.setRecommendedStaff(Integer.parseInt(recommendedStaff));
            request.setCategoryId(Integer.parseInt(categoryId));
            
            if (estimatedDurationHours != null && !estimatedDurationHours.isEmpty()) {
                request.setEstimatedDurationHours(new java.math.BigDecimal(estimatedDurationHours));
            }
            
            // Upload icon to Cloudinary if provided
            if (icon != null && !icon.isEmpty()) {
                Map<String, Object> uploadResult = cloudinaryService.upload(icon, "services/icons");
                String iconUrl = (String) uploadResult.get("secureUrl");
                request.setIconUrl(iconUrl);
            }
            
            ServiceAdminData service = adminServiceManagementService.createService(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Tạo dịch vụ thành công",
                    "data", service
            ));
        } catch (NumberFormatException e) {
            log.error("Invalid number format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Dữ liệu số không hợp lệ"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating service: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating service with icon: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi tạo dịch vụ"
            ));
        }
    }

    /**
     * Update service with icon upload
     * PUT /api/v1/admin/services/{serviceId}
     */
    @PutMapping(value = "/{serviceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateServiceWithIcon(
            @PathVariable Integer serviceId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "basePrice", required = false) String basePrice,
            @RequestParam(value = "unit", required = false) String unit,
            @RequestParam(value = "recommendedStaff", required = false) String recommendedStaff,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "estimatedDurationHours", required = false) String estimatedDurationHours,
            @RequestParam(value = "isActive", required = false) String isActive,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        try {
            UpdateServiceRequest request = new UpdateServiceRequest();
            
            if (name != null && !name.isEmpty()) {
                request.setName(name);
            }
            if (description != null) {
                request.setDescription(description);
            }
            if (basePrice != null && !basePrice.isEmpty()) {
                request.setBasePrice(new java.math.BigDecimal(basePrice));
            }
            if (unit != null && !unit.isEmpty()) {
                request.setUnit(unit);
            }
            if (recommendedStaff != null && !recommendedStaff.isEmpty()) {
                request.setRecommendedStaff(Integer.parseInt(recommendedStaff));
            }
            if (categoryId != null && !categoryId.isEmpty()) {
                request.setCategoryId(Integer.parseInt(categoryId));
            }
            if (estimatedDurationHours != null && !estimatedDurationHours.isEmpty()) {
                request.setEstimatedDurationHours(new java.math.BigDecimal(estimatedDurationHours));
            }
            if (isActive != null && !isActive.isEmpty()) {
                request.setIsActive(Boolean.parseBoolean(isActive));
            }
            
            // Upload new icon to Cloudinary if provided
            if (icon != null && !icon.isEmpty()) {
                Map<String, Object> uploadResult = cloudinaryService.upload(icon, "services/icons");
                String iconUrl = (String) uploadResult.get("secureUrl");
                request.setIconUrl(iconUrl);
            }
            
            ServiceAdminData service = adminServiceManagementService.updateService(serviceId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật dịch vụ thành công",
                    "data", service
            ));
        } catch (NumberFormatException e) {
            log.error("Invalid number format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Dữ liệu số không hợp lệ"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating service: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating service with icon: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi cập nhật dịch vụ"
            ));
        }
    }

    /**
     * Delete service (soft delete - set isActive = false)
     * DELETE /api/v1/admin/services/{serviceId}
     */
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable Integer serviceId) {
        try {
            adminServiceManagementService.deleteService(serviceId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa dịch vụ thành công"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi xóa dịch vụ"
            ));
        }
    }

    /**
     * Activate service
     * PATCH /api/v1/admin/services/{serviceId}/activate
     */
    @PatchMapping("/{serviceId}/activate")
    public ResponseEntity<?> activateService(@PathVariable Integer serviceId) {
        try {
            ServiceAdminData service = adminServiceManagementService.activateService(serviceId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kích hoạt dịch vụ thành công",
                    "data", service
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error activating service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error activating service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi kích hoạt dịch vụ"
            ));
        }
    }

    // ===================== SERVICE OPTION ENDPOINTS =====================

    /**
     * Get all options for a service
     * GET /api/v1/admin/services/{serviceId}/options
     */
    @GetMapping("/{serviceId}/options")
    public ResponseEntity<?> getServiceOptions(@PathVariable Integer serviceId) {
        try {
            List<ServiceOptionAdminData> options = adminServiceManagementService.getServiceOptions(serviceId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", options
            ));
        } catch (Exception e) {
            log.error("Error fetching service options: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy danh sách tùy chọn dịch vụ"
            ));
        }
    }

    /**
     * Get option by ID
     * GET /api/v1/admin/services/options/{optionId}
     */
    @GetMapping("/options/{optionId}")
    public ResponseEntity<?> getServiceOptionById(@PathVariable Integer optionId) {
        try {
            ServiceOptionAdminData option = adminServiceManagementService.getServiceOptionById(optionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", option
            ));
        } catch (IllegalArgumentException e) {
            log.error("Service option not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching service option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy thông tin tùy chọn dịch vụ"
            ));
        }
    }

    /**
     * Create service option
     * POST /api/v1/admin/services/options
     */
    @PostMapping("/options")
    public ResponseEntity<?> createServiceOption(@Valid @RequestBody CreateServiceOptionRequest request) {
        try {
            ServiceOptionAdminData option = adminServiceManagementService.createServiceOption(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Tạo tùy chọn dịch vụ thành công",
                    "data", option
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating service option: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating service option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi tạo tùy chọn dịch vụ"
            ));
        }
    }

    /**
     * Update service option
     * PUT /api/v1/admin/services/options/{optionId}
     */
    @PutMapping("/options/{optionId}")
    public ResponseEntity<?> updateServiceOption(
            @PathVariable Integer optionId,
            @Valid @RequestBody UpdateServiceOptionRequest request) {
        try {
            ServiceOptionAdminData option = adminServiceManagementService.updateServiceOption(optionId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật tùy chọn dịch vụ thành công",
                    "data", option
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating service option: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating service option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi cập nhật tùy chọn dịch vụ"
            ));
        }
    }

    /**
     * Delete service option
     * DELETE /api/v1/admin/services/options/{optionId}
     */
    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<?> deleteServiceOption(@PathVariable Integer optionId) {
        try {
            adminServiceManagementService.deleteServiceOption(optionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa tùy chọn dịch vụ thành công"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting service option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting service option: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi xóa tùy chọn dịch vụ"
            ));
        }
    }

    // ===================== SERVICE OPTION CHOICE ENDPOINTS =====================

    /**
     * Get all choices for an option
     * GET /api/v1/admin/services/options/{optionId}/choices
     */
    @GetMapping("/options/{optionId}/choices")
    public ResponseEntity<?> getServiceOptionChoices(@PathVariable Integer optionId) {
        try {
            List<ServiceOptionChoiceAdminData> choices = adminServiceManagementService.getServiceOptionChoices(optionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", choices
            ));
        } catch (Exception e) {
            log.error("Error fetching service option choices: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy danh sách lựa chọn"
            ));
        }
    }

    /**
     * Get choice by ID
     * GET /api/v1/admin/services/choices/{choiceId}
     */
    @GetMapping("/choices/{choiceId}")
    public ResponseEntity<?> getServiceOptionChoiceById(@PathVariable Integer choiceId) {
        try {
            ServiceOptionChoiceAdminData choice = adminServiceManagementService.getServiceOptionChoiceById(choiceId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", choice
            ));
        } catch (IllegalArgumentException e) {
            log.error("Service option choice not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching service option choice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy thông tin lựa chọn"
            ));
        }
    }

    /**
     * Create service option choice
     * POST /api/v1/admin/services/choices
     */
    @PostMapping("/choices")
    public ResponseEntity<?> createServiceOptionChoice(@Valid @RequestBody CreateServiceOptionChoiceRequest request) {
        try {
            ServiceOptionChoiceAdminData choice = adminServiceManagementService.createServiceOptionChoice(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Tạo lựa chọn thành công",
                    "data", choice
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating service option choice: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating service option choice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi tạo lựa chọn"
            ));
        }
    }

    /**
     * Update service option choice
     * PUT /api/v1/admin/services/choices/{choiceId}
     */
    @PutMapping("/choices/{choiceId}")
    public ResponseEntity<?> updateServiceOptionChoice(
            @PathVariable Integer choiceId,
            @Valid @RequestBody UpdateServiceOptionChoiceRequest request) {
        try {
            ServiceOptionChoiceAdminData choice = adminServiceManagementService.updateServiceOptionChoice(choiceId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật lựa chọn thành công",
                    "data", choice
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating service option choice: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating service option choice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi cập nhật lựa chọn"
            ));
        }
    }

    /**
     * Delete service option choice
     * DELETE /api/v1/admin/services/choices/{choiceId}
     */
    @DeleteMapping("/choices/{choiceId}")
    public ResponseEntity<?> deleteServiceOptionChoice(@PathVariable Integer choiceId) {
        try {
            adminServiceManagementService.deleteServiceOptionChoice(choiceId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa lựa chọn thành công"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting service option choice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting service option choice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi xóa lựa chọn"
            ));
        }
    }

    // ===================== PRICING RULE ENDPOINTS =====================

    /**
     * Get all pricing rules for a service
     * GET /api/v1/admin/services/{serviceId}/pricing-rules
     */
    @GetMapping("/{serviceId}/pricing-rules")
    public ResponseEntity<?> getPricingRules(@PathVariable Integer serviceId) {
        try {
            List<PricingRuleAdminData> rules = adminServiceManagementService.getPricingRules(serviceId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", rules
            ));
        } catch (Exception e) {
            log.error("Error fetching pricing rules: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy danh sách quy tắc giá"
            ));
        }
    }

    /**
     * Get pricing rule by ID
     * GET /api/v1/admin/services/pricing-rules/{ruleId}
     */
    @GetMapping("/pricing-rules/{ruleId}")
    public ResponseEntity<?> getPricingRuleById(@PathVariable Integer ruleId) {
        try {
            PricingRuleAdminData rule = adminServiceManagementService.getPricingRuleById(ruleId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", rule
            ));
        } catch (IllegalArgumentException e) {
            log.error("Pricing rule not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching pricing rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi lấy thông tin quy tắc giá"
            ));
        }
    }

    /**
     * Create pricing rule
     * POST /api/v1/admin/services/pricing-rules
     */
    @PostMapping("/pricing-rules")
    public ResponseEntity<?> createPricingRule(@Valid @RequestBody CreatePricingRuleRequest request) {
        try {
            PricingRuleAdminData rule = adminServiceManagementService.createPricingRule(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Tạo quy tắc giá thành công",
                    "data", rule
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating pricing rule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating pricing rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi tạo quy tắc giá"
            ));
        }
    }

    /**
     * Update pricing rule
     * PUT /api/v1/admin/services/pricing-rules/{ruleId}
     */
    @PutMapping("/pricing-rules/{ruleId}")
    public ResponseEntity<?> updatePricingRule(
            @PathVariable Integer ruleId,
            @Valid @RequestBody UpdatePricingRuleRequest request) {
        try {
            PricingRuleAdminData rule = adminServiceManagementService.updatePricingRule(ruleId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật quy tắc giá thành công",
                    "data", rule
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating pricing rule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating pricing rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi cập nhật quy tắc giá"
            ));
        }
    }

    /**
     * Delete pricing rule
     * DELETE /api/v1/admin/services/pricing-rules/{ruleId}
     */
    @DeleteMapping("/pricing-rules/{ruleId}")
    public ResponseEntity<?> deletePricingRule(@PathVariable Integer ruleId) {
        try {
            adminServiceManagementService.deletePricingRule(ruleId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa quy tắc giá thành công"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting pricing rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting pricing rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đã xảy ra lỗi khi xóa quy tắc giá"
            ));
        }
    }
}
