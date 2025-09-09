package iuh.house_keeping_service_be.services.ServiceService.impl;

import iuh.house_keeping_service_be.dtos.Service.*;
import iuh.house_keeping_service_be.enums.ConditionLogic;
import iuh.house_keeping_service_be.models.PricingRule;
import iuh.house_keeping_service_be.models.Service;
import iuh.house_keeping_service_be.models.ServiceOption;
import iuh.house_keeping_service_be.models.ServiceOptionChoice;
import iuh.house_keeping_service_be.repositories.PricingRuleRepository;
import iuh.house_keeping_service_be.repositories.RuleConditionRepository;
import iuh.house_keeping_service_be.repositories.ServiceOptionRepository;
import iuh.house_keeping_service_be.repositories.ServiceRepository;
import iuh.house_keeping_service_be.services.ServiceService.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceOptionRepository serviceOptionRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final RuleConditionRepository ruleConditionRepository;
    private final DecimalFormat priceFormatter = new DecimalFormat("#,###");

    @Override
    @Transactional(readOnly = true)
    public ServiceListResponse getAllActiveServices() {
        try {
            List<Service> services = serviceRepository.findAllActiveServices();

            List<ServiceData> serviceDataList = services.stream()
                    .map(this::convertToServiceData)
                    .collect(Collectors.toList());

            return new ServiceListResponse(
                    true,
                    serviceDataList.isEmpty() ? "Hiện tại chưa có dịch vụ nào" : "Lấy danh sách dịch vụ thành công",
                    serviceDataList
            );

        } catch (Exception e) {
            log.error("Error getting all active services: {}", e.getMessage());
            return new ServiceListResponse(
                    false,
                    "Lỗi khi lấy danh sách dịch vụ",
                    null
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDetailResponse getServiceDetail(Integer serviceId) {
        try {
            Optional<Service> serviceOpt = serviceRepository.findActiveServiceById(serviceId);

            if (serviceOpt.isEmpty()) {
                return new ServiceDetailResponse(
                        false,
                        "Không tìm thấy dịch vụ hoặc dịch vụ đã ngừng hoạt động",
                        null
                );
            }

            Service service = serviceOpt.get();
            ServiceDetailData serviceDetailData = convertToServiceDetailData(service);

            return new ServiceDetailResponse(
                    true,
                    "Lấy thông tin dịch vụ thành công",
                    serviceDetailData
            );

        } catch (Exception e) {
            log.error("Error getting service detail for id {}: {}", serviceId, e.getMessage());
            return new ServiceDetailResponse(
                    false,
                    "Lỗi khi lấy thông tin dịch vụ",
                    null
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceListResponse searchServices(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllActiveServices();
            }

            List<Service> services = serviceRepository.searchActiveServicesByName(keyword.trim());

            List<ServiceData> serviceDataList = services.stream()
                    .map(this::convertToServiceData)
                    .collect(Collectors.toList());

            String message = serviceDataList.isEmpty()
                    ? "Không tìm thấy dịch vụ nào phù hợp với từ khóa: " + keyword
                    : "Tìm thấy " + serviceDataList.size() + " dịch vụ phù hợp";

            return new ServiceListResponse(
                    true,
                    message,
                    serviceDataList
            );

        } catch (Exception e) {
            log.error("Error searching services with keyword '{}': {}", keyword, e.getMessage());
            return new ServiceListResponse(
                    false,
                    "Lỗi khi tìm kiếm dịch vụ",
                    null
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countActiveServices() {
        try {
            return serviceRepository.countActiveServices();
        } catch (Exception e) {
            log.error("Error counting active services: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOptionsResponse getServiceOptions(Integer serviceId) {
        try {
            // Kiểm tra service tồn tại và đang hoạt động
            Optional<Service> serviceOpt = serviceRepository.findActiveServiceById(serviceId);

            if (serviceOpt.isEmpty()) {
                return new ServiceOptionsResponse(
                        false,
                        "Không tìm thấy dịch vụ hoặc dịch vụ đã ngừng hoạt động",
                        null
                );
            }

            Service service = serviceOpt.get();

            // Lấy service options với choices
            List<ServiceOption> serviceOptions = serviceOptionRepository.findByServiceIdWithChoices(serviceId);

            // Convert to DTOs
            List<ServiceOptionData> optionDataList = serviceOptions.stream()
                    .map(this::convertToServiceOptionData)
                    .collect(Collectors.toList());

            ServiceOptionsData serviceOptionsData = new ServiceOptionsData(
                    service.getServiceId(),
                    service.getName(),
                    service.getDescription(),
                    service.getBasePrice(),
                    service.getUnit(),
                    service.getEstimatedDurationHours(),
                    formatPrice(service.getBasePrice(), service.getUnit()),
                    formatDuration(service.getEstimatedDurationHours()),
                    optionDataList
            );

            String message = optionDataList.isEmpty()
                    ? "Dịch vụ này không có tùy chọn nào"
                    : "Lấy thông tin dịch vụ và tùy chọn thành công";

            return new ServiceOptionsResponse(
                    true,
                    message,
                    serviceOptionsData
            );

        } catch (Exception e) {
            log.error("Error getting service options for serviceId {}: {}", serviceId, e.getMessage());
            return new ServiceOptionsResponse(
                    false,
                    "Lỗi khi lấy thông tin dịch vụ và tùy chọn",
                    null
            );
        }
    }

    private ServiceOptionData convertToServiceOptionData(ServiceOption serviceOption) {
        List<ServiceOptionChoiceData> choiceDataList = serviceOption.getChoices().stream()
                .map(this::convertToServiceOptionChoiceData)
                .sorted((c1, c2) -> Integer.compare(
                        c1.displayOrder() != null ? c1.displayOrder() : 0,
                        c2.displayOrder() != null ? c2.displayOrder() : 0
                ))
                .collect(Collectors.toList());

        return new ServiceOptionData(
                serviceOption.getId(),
                serviceOption.getLabel(),
                serviceOption.getOptionType().name(),
                serviceOption.getDisplayOrder(),
                serviceOption.getIsRequired(),
                choiceDataList
        );
    }

    private ServiceOptionChoiceData convertToServiceOptionChoiceData(ServiceOptionChoice choice) {
        return new ServiceOptionChoiceData(
                choice.getId(),
                choice.getLabel(),
                choice.getDisplayOrder(),
                choice.getIsDefault()
        );
    }

    private ServiceData convertToServiceData(Service service) {
        return new ServiceData(
                service.getServiceId(),
                service.getName(),
                service.getDescription(),
                service.getBasePrice(),
                service.getUnit(),
                service.getEstimatedDurationHours(),
                service.getIsActive()
        );
    }

    private ServiceDetailData convertToServiceDetailData(Service service) {
        String formattedPrice = formatPrice(service.getBasePrice(), service.getUnit());
        String formattedDuration = formatDuration(service.getEstimatedDurationHours());

        return new ServiceDetailData(
                service.getServiceId(),
                service.getName(),
                service.getDescription(),
                service.getBasePrice(),
                service.getUnit(),
                service.getEstimatedDurationHours(),
                service.getIsActive(),
                formattedPrice,
                formattedDuration
        );
    }

    private String formatPrice(BigDecimal price, String unit) {
        if (price == null) return "Liên hệ";

        String formattedPrice = priceFormatter.format(price.longValue()) + "đ";

        return switch (unit.toLowerCase()) {
            case "hour" -> formattedPrice + "/giờ";
            case "m2" -> formattedPrice + "/m²";
            case "package" -> formattedPrice + "/gói";
            default -> formattedPrice + "/" + unit;
        };
    }

    private String formatDuration(BigDecimal hours) {
        if (hours == null) return "Chưa xác định";

        if (hours.compareTo(BigDecimal.ONE) < 0) {
            int minutes = hours.multiply(BigDecimal.valueOf(60)).intValue();
            return minutes + " phút";
        } else if (hours.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO)) {
            return hours.intValue() + " giờ";
        } else {
            int totalMinutes = hours.multiply(BigDecimal.valueOf(60)).intValue();
            int h = totalMinutes / 60;
            int m = totalMinutes % 60;
            return h + " giờ " + m + " phút";
        }
    }

    @Override
    public CalculatePriceResponse calculatePrice(CalculatePriceRequest request) {
        try {
            // Validate request
            if (request.serviceId() == null || request.selectedChoiceIds() == null) {
                return new CalculatePriceResponse(false, "Thông tin yêu cầu không hợp lệ", null);
            }

            // Get service
            Optional<Service> serviceOpt = serviceRepository.findById(request.serviceId());
            if (serviceOpt.isEmpty()) {
                return new CalculatePriceResponse(false, "Không tìm thấy dịch vụ", null);
            }

            Service service = serviceOpt.get();
            BigDecimal basePrice = service.getBasePrice();
            BigDecimal totalPriceAdjustment = BigDecimal.ZERO;
            int totalStaffAdjustment = 0;
            BigDecimal totalDurationAdjustment = BigDecimal.ZERO;

            // Get applicable pricing rules
            List<PricingRule> applicableRules = findApplicableRules(request.serviceId(), request.selectedChoiceIds());

            // Apply pricing rules
            for (PricingRule rule : applicableRules) {
                if (rule.getPriceAdjustment() != null) {
                    totalPriceAdjustment = totalPriceAdjustment.add(rule.getPriceAdjustment());
                }
                if (rule.getStaffAdjustment() != null) {
                    totalStaffAdjustment += rule.getStaffAdjustment();
                }
                if (rule.getDurationAdjustmentHours() != null) {
                    totalDurationAdjustment = totalDurationAdjustment.add(rule.getDurationAdjustmentHours());
                }
            }

            // Calculate final values
            BigDecimal finalPrice = basePrice.add(totalPriceAdjustment);
            if (request.quantity() != null && request.quantity() > 1) {
                finalPrice = finalPrice.multiply(BigDecimal.valueOf(request.quantity()));
            }

            Integer suggestedStaff = Math.max(1, 1 + totalStaffAdjustment);

            BigDecimal estimatedDuration = service.getEstimatedDurationHours() != null
                ? service.getEstimatedDurationHours().add(totalDurationAdjustment)
                : BigDecimal.valueOf(2.0).add(totalDurationAdjustment);

            // Format values
            DecimalFormat priceFormat = new DecimalFormat("#,###");
            String formattedPrice = priceFormat.format(finalPrice) + "đ";
            String formattedDuration = formatDuration(estimatedDuration);

            CalculatedPriceData data = new CalculatedPriceData(
                service.getServiceId(),
                service.getName(),
                basePrice,
                totalPriceAdjustment,
                finalPrice,
                suggestedStaff,
                estimatedDuration,
                formattedPrice,
                formattedDuration
            );

            return new CalculatePriceResponse(true, "Tính toán giá thành công", data);

        } catch (Exception e) {
            log.error("Error calculating price: {}", e.getMessage(), e);
            return new CalculatePriceResponse(false, "Lỗi hệ thống khi tính toán giá", null);
        }
    }

    private List<PricingRule> findApplicableRules(Integer serviceId, List<Integer> selectedChoiceIds) {
        List<PricingRule> allRules = pricingRuleRepository.findByServiceIdOrderByPriorityDesc(serviceId);
        List<PricingRule> applicableRules = new ArrayList<>();

        for (PricingRule rule : allRules) {
            if (isRuleApplicable(rule, selectedChoiceIds)) {
                applicableRules.add(rule);
            }
        }

        return applicableRules;
    }

    private boolean isRuleApplicable(PricingRule rule, List<Integer> selectedChoiceIds) {
        List<Integer> requiredChoiceIds = ruleConditionRepository.findChoiceIdsByRuleId(rule.getId());

        if (requiredChoiceIds.isEmpty()) {
            return false;
        }

        if (rule.getConditionLogic() == ConditionLogic.ALL) {
            // Tất cả điều kiện phải thỏa mãn
            return new HashSet<>(selectedChoiceIds).containsAll(requiredChoiceIds);
        } else {
            // Chỉ cần một điều kiện thỏa mãn
            return requiredChoiceIds.stream().anyMatch(selectedChoiceIds::contains);
        }
    }
}