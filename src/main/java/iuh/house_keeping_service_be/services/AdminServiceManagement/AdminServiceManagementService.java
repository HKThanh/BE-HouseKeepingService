package iuh.house_keeping_service_be.services.AdminServiceManagement;

import iuh.house_keeping_service_be.dtos.Service.Admin.*;
import iuh.house_keeping_service_be.exceptions.ResourceNotFoundException;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminServiceManagementService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Autowired
    private ServiceOptionRepository serviceOptionRepository;

    @Autowired
    private ServiceOptionChoiceRepository serviceOptionChoiceRepository;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    // ===================== SERVICE MANAGEMENT =====================

    /**
     * Get all services with pagination
     */
    public Page<ServiceAdminData> getAllServices(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<iuh.house_keeping_service_be.models.Service> services = serviceRepository.findAll(pageable);
        
        return services.map(this::convertToServiceAdminData);
    }

    /**
     * Get service by ID
     */
    public ServiceAdminData getServiceById(Integer serviceId) {
        iuh.house_keeping_service_be.models.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service không tồn tại với ID: " + serviceId));
        
        return convertToServiceAdminData(service);
    }

    /**
     * Create new service
     */
    @Transactional
    public ServiceAdminData createService(CreateServiceRequest request) {
        // Validate category exists
        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + request.getCategoryId()));

        iuh.house_keeping_service_be.models.Service service = new iuh.house_keeping_service_be.models.Service();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setUnit(request.getUnit());
        service.setEstimatedDurationHours(request.getEstimatedDurationHours());
        service.setRecommendedStaff(request.getRecommendedStaff());
        service.setIconUrl(request.getIconUrl());
        service.setCategory(category);
        service.setIsActive(true);

        service = serviceRepository.save(service);
        log.info("Created new service: {}", service.getName());
        
        return convertToServiceAdminData(service);
    }

    /**
     * Update service
     */
    @Transactional
    public ServiceAdminData updateService(Integer serviceId, UpdateServiceRequest request) {
        iuh.house_keeping_service_be.models.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service không tồn tại với ID: " + serviceId));

        if (request.getName() != null) {
            service.setName(request.getName());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            service.setBasePrice(request.getBasePrice());
        }
        if (request.getUnit() != null) {
            service.setUnit(request.getUnit());
        }
        if (request.getEstimatedDurationHours() != null) {
            service.setEstimatedDurationHours(request.getEstimatedDurationHours());
        }
        if (request.getRecommendedStaff() != null) {
            service.setRecommendedStaff(request.getRecommendedStaff());
        }
        if (request.getIconUrl() != null) {
            service.setIconUrl(request.getIconUrl());
        }
        if (request.getCategoryId() != null) {
            ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + request.getCategoryId()));
            service.setCategory(category);
        }
        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }

        service = serviceRepository.save(service);
        log.info("Updated service: {}", service.getName());
        
        return convertToServiceAdminData(service);
    }

    /**
     * Soft delete service (set isActive = false)
     */
    @Transactional
    public void deleteService(Integer serviceId) {
        iuh.house_keeping_service_be.models.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service không tồn tại với ID: " + serviceId));

        service.setIsActive(false);
        serviceRepository.save(service);
        log.info("Soft deleted service: {}", service.getName());
    }

    /**
     * Activate service
     */
    @Transactional
    public ServiceAdminData activateService(Integer serviceId) {
        iuh.house_keeping_service_be.models.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service không tồn tại với ID: " + serviceId));

        service.setIsActive(true);
        service = serviceRepository.save(service);
        log.info("Activated service: {}", service.getName());
        
        return convertToServiceAdminData(service);
    }

    // ===================== SERVICE OPTION MANAGEMENT =====================

    /**
     * Get all options for a service
     */
    public List<ServiceOptionAdminData> getServiceOptions(Integer serviceId) {
        List<ServiceOption> options = serviceOptionRepository.findByServiceIdWithChoices(serviceId);
        return options.stream()
                .map(this::convertToServiceOptionAdminData)
                .collect(Collectors.toList());
    }

    /**
     * Get option by ID
     */
    public ServiceOptionAdminData getServiceOptionById(Integer optionId) {
        ServiceOption option = serviceOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Service Option không tồn tại với ID: " + optionId));
        
        return convertToServiceOptionAdminData(option);
    }

    /**
     * Create service option
     */
    @Transactional
    public ServiceOptionAdminData createServiceOption(CreateServiceOptionRequest request) {
        iuh.house_keeping_service_be.models.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service không tồn tại với ID: " + request.getServiceId()));

        ServiceOption option = new ServiceOption();
        option.setService(service);
        option.setLabel(request.getLabel());
        option.setOptionType(request.getOptionType());
        option.setDisplayOrder(request.getDisplayOrder());
        option.setIsRequired(request.getIsRequired());
        option.setValidationRules(request.getValidationRules());

        if (request.getParentOptionId() != null) {
            ServiceOption parentOption = serviceOptionRepository.findById(request.getParentOptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Option không tồn tại với ID: " + request.getParentOptionId()));
            option.setParentOption(parentOption);
        }

        if (request.getParentChoiceId() != null) {
            ServiceOptionChoice parentChoice = serviceOptionChoiceRepository.findById(request.getParentChoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Choice không tồn tại với ID: " + request.getParentChoiceId()));
            option.setParentChoice(parentChoice);
        }

        option = serviceOptionRepository.save(option);
        log.info("Created new service option: {}", option.getLabel());
        
        return convertToServiceOptionAdminData(option);
    }

    /**
     * Update service option
     */
    @Transactional
    public ServiceOptionAdminData updateServiceOption(Integer optionId, UpdateServiceOptionRequest request) {
        ServiceOption option = serviceOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Service Option không tồn tại với ID: " + optionId));

        if (request.getLabel() != null) {
            option.setLabel(request.getLabel());
        }
        if (request.getOptionType() != null) {
            option.setOptionType(request.getOptionType());
        }
        if (request.getDisplayOrder() != null) {
            option.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsRequired() != null) {
            option.setIsRequired(request.getIsRequired());
        }
        if (request.getIsActive() != null) {
            option.setIsActive(request.getIsActive());
        }
        if (request.getValidationRules() != null) {
            option.setValidationRules(request.getValidationRules());
        }

        if (request.getParentOptionId() != null) {
            ServiceOption parentOption = serviceOptionRepository.findById(request.getParentOptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Option không tồn tại với ID: " + request.getParentOptionId()));
            option.setParentOption(parentOption);
        }

        if (request.getParentChoiceId() != null) {
            ServiceOptionChoice parentChoice = serviceOptionChoiceRepository.findById(request.getParentChoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Choice không tồn tại với ID: " + request.getParentChoiceId()));
            option.setParentChoice(parentChoice);
        }

        option = serviceOptionRepository.save(option);
        log.info("Updated service option: {}", option.getLabel());
        
        return convertToServiceOptionAdminData(option);
    }

    /**
     * Delete service option
     */
    @Transactional
    public void deleteServiceOption(Integer optionId) {
        ServiceOption option = serviceOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Service Option không tồn tại với ID: " + optionId));

        serviceOptionRepository.delete(option);
        log.info("Deleted service option: {}", option.getLabel());
    }

    // ===================== SERVICE OPTION CHOICE MANAGEMENT =====================

    /**
     * Get all choices for an option
     */
    public List<ServiceOptionChoiceAdminData> getServiceOptionChoices(Integer optionId) {
        List<ServiceOptionChoice> choices = serviceOptionChoiceRepository.findByServiceOptionIdOrderByDisplayOrder(optionId);
        return choices.stream()
                .map(this::convertToServiceOptionChoiceAdminData)
                .collect(Collectors.toList());
    }

    /**
     * Get choice by ID
     */
    public ServiceOptionChoiceAdminData getServiceOptionChoiceById(Integer choiceId) {
        ServiceOptionChoice choice = serviceOptionChoiceRepository.findById(choiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service Option Choice không tồn tại với ID: " + choiceId));
        
        return convertToServiceOptionChoiceAdminData(choice);
    }

    /**
     * Create service option choice
     */
    @Transactional
    public ServiceOptionChoiceAdminData createServiceOptionChoice(CreateServiceOptionChoiceRequest request) {
        ServiceOption option = serviceOptionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Service Option không tồn tại với ID: " + request.getOptionId()));

        ServiceOptionChoice choice = new ServiceOptionChoice();
        choice.setOption(option);
        choice.setLabel(request.getLabel());
        choice.setIsDefault(request.getIsDefault());
        choice.setDisplayOrder(request.getDisplayOrder());

        choice = serviceOptionChoiceRepository.save(choice);
        log.info("Created new service option choice: {}", choice.getLabel());
        
        return convertToServiceOptionChoiceAdminData(choice);
    }

    /**
     * Update service option choice
     */
    @Transactional
    public ServiceOptionChoiceAdminData updateServiceOptionChoice(Integer choiceId, UpdateServiceOptionChoiceRequest request) {
        ServiceOptionChoice choice = serviceOptionChoiceRepository.findById(choiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service Option Choice không tồn tại với ID: " + choiceId));

        if (request.getLabel() != null) {
            choice.setLabel(request.getLabel());
        }
        if (request.getIsDefault() != null) {
            choice.setIsDefault(request.getIsDefault());
        }
        if (request.getIsActive() != null) {
            choice.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            choice.setDisplayOrder(request.getDisplayOrder());
        }

        choice = serviceOptionChoiceRepository.save(choice);
        log.info("Updated service option choice: {}", choice.getLabel());
        
        return convertToServiceOptionChoiceAdminData(choice);
    }

    /**
     * Delete service option choice
     */
    @Transactional
    public void deleteServiceOptionChoice(Integer choiceId) {
        ServiceOptionChoice choice = serviceOptionChoiceRepository.findById(choiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service Option Choice không tồn tại với ID: " + choiceId));

        serviceOptionChoiceRepository.delete(choice);
        log.info("Deleted service option choice: {}", choice.getLabel());
    }

    // ===================== PRICING RULE MANAGEMENT =====================

    /**
     * Get all pricing rules for a service
     */
    public List<PricingRuleAdminData> getPricingRules(Integer serviceId) {
        List<PricingRule> rules = pricingRuleRepository.findByServiceIdOrderByPriorityDesc(serviceId);
        return rules.stream()
                .map(this::convertToPricingRuleAdminData)
                .collect(Collectors.toList());
    }

    /**
     * Get pricing rule by ID
     */
    public PricingRuleAdminData getPricingRuleById(Integer ruleId) {
        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing Rule không tồn tại với ID: " + ruleId));
        
        return convertToPricingRuleAdminData(rule);
    }

    /**
     * Create pricing rule
     */
    @Transactional
    public PricingRuleAdminData createPricingRule(CreatePricingRuleRequest request) {
        iuh.house_keeping_service_be.models.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service không tồn tại với ID: " + request.getServiceId()));

        PricingRule rule = new PricingRule();
        rule.setService(service);
        rule.setRuleName(request.getRuleName());
        rule.setConditionLogic(request.getConditionLogic());
        rule.setPriority(request.getPriority());
        rule.setPriceAdjustment(request.getPriceAdjustment());
        rule.setStaffAdjustment(request.getStaffAdjustment());
        rule.setDurationAdjustmentHours(request.getDurationAdjustmentHours());

        rule = pricingRuleRepository.save(rule);
        log.info("Created new pricing rule: {}", rule.getRuleName());
        
        return convertToPricingRuleAdminData(rule);
    }

    /**
     * Update pricing rule
     */
    @Transactional
    public PricingRuleAdminData updatePricingRule(Integer ruleId, UpdatePricingRuleRequest request) {
        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing Rule không tồn tại với ID: " + ruleId));

        if (request.getRuleName() != null) {
            rule.setRuleName(request.getRuleName());
        }
        if (request.getConditionLogic() != null) {
            rule.setConditionLogic(request.getConditionLogic());
        }
        if (request.getPriority() != null) {
            rule.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }
        if (request.getPriceAdjustment() != null) {
            rule.setPriceAdjustment(request.getPriceAdjustment());
        }
        if (request.getStaffAdjustment() != null) {
            rule.setStaffAdjustment(request.getStaffAdjustment());
        }
        if (request.getDurationAdjustmentHours() != null) {
            rule.setDurationAdjustmentHours(request.getDurationAdjustmentHours());
        }

        rule = pricingRuleRepository.save(rule);
        log.info("Updated pricing rule: {}", rule.getRuleName());
        
        return convertToPricingRuleAdminData(rule);
    }

    /**
     * Delete pricing rule
     */
    @Transactional
    public void deletePricingRule(Integer ruleId) {
        PricingRule rule = pricingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing Rule không tồn tại với ID: " + ruleId));

        pricingRuleRepository.delete(rule);
        log.info("Deleted pricing rule: {}", rule.getRuleName());
    }

    // ===================== CONVERTER METHODS =====================

    private ServiceAdminData convertToServiceAdminData(iuh.house_keeping_service_be.models.Service service) {
        ServiceAdminData data = new ServiceAdminData();
        data.setServiceId(service.getServiceId());
        data.setName(service.getName());
        data.setDescription(service.getDescription());
        data.setBasePrice(service.getBasePrice());
        data.setUnit(service.getUnit());
        data.setEstimatedDurationHours(service.getEstimatedDurationHours());
        data.setRecommendedStaff(service.getRecommendedStaff());
        data.setIconUrl(service.getIconUrl());
        data.setIsActive(service.getIsActive());
        
        if (service.getCategory() != null) {
            data.setCategoryId(service.getCategory().getCategoryId());
            data.setCategoryName(service.getCategory().getCategoryName());
        }
        
        // Count related entities
        if (service.getServiceOptions() != null) {
            data.setOptionsCount(service.getServiceOptions().size());
        } else {
            data.setOptionsCount(0);
        }
        
        // Count pricing rules
        List<PricingRule> rules = pricingRuleRepository.findByServiceIdOrderByPriorityDesc(service.getServiceId());
        data.setPricingRulesCount(rules.size());
        
        return data;
    }

    private ServiceOptionAdminData convertToServiceOptionAdminData(ServiceOption option) {
        ServiceOptionAdminData data = new ServiceOptionAdminData();
        data.setOptionId(option.getId());
        data.setServiceId(option.getService().getServiceId());
        data.setServiceName(option.getService().getName());
        data.setLabel(option.getLabel());
        data.setOptionType(option.getOptionType());
        data.setDisplayOrder(option.getDisplayOrder());
        data.setIsRequired(option.getIsRequired());
        data.setIsActive(option.getIsActive());
        data.setValidationRules(option.getValidationRules());
        
        if (option.getParentOption() != null) {
            data.setParentOptionId(option.getParentOption().getId());
        }
        
        if (option.getParentChoice() != null) {
            data.setParentChoiceId(option.getParentChoice().getId());
        }
        
        if (option.getChoices() != null) {
            List<ServiceOptionChoiceAdminData> choices = option.getChoices().stream()
                    .map(this::convertToServiceOptionChoiceAdminData)
                    .collect(Collectors.toList());
            data.setChoices(choices);
        } else {
            data.setChoices(new ArrayList<>());
        }
        
        return data;
    }

    private ServiceOptionChoiceAdminData convertToServiceOptionChoiceAdminData(ServiceOptionChoice choice) {
        ServiceOptionChoiceAdminData data = new ServiceOptionChoiceAdminData();
        data.setChoiceId(choice.getId());
        data.setOptionId(choice.getOption().getId());
        data.setLabel(choice.getLabel());
        data.setIsDefault(choice.getIsDefault());
        data.setIsActive(choice.getIsActive());
        data.setDisplayOrder(choice.getDisplayOrder());
        return data;
    }

    private PricingRuleAdminData convertToPricingRuleAdminData(PricingRule rule) {
        PricingRuleAdminData data = new PricingRuleAdminData();
        data.setRuleId(rule.getId());
        data.setServiceId(rule.getService().getServiceId());
        data.setServiceName(rule.getService().getName());
        data.setRuleName(rule.getRuleName());
        data.setConditionLogic(rule.getConditionLogic());
        data.setPriority(rule.getPriority());
        data.setIsActive(rule.getIsActive());
        data.setPriceAdjustment(rule.getPriceAdjustment());
        data.setStaffAdjustment(rule.getStaffAdjustment());
        data.setDurationAdjustmentHours(rule.getDurationAdjustmentHours());
        return data;
    }
}
