package iuh.house_keeping_service_be.services.ServiceService.impl;

import iuh.house_keeping_service_be.dtos.Service.*;
import iuh.house_keeping_service_be.models.Service;
import iuh.house_keeping_service_be.repositories.ServiceRepository;
import iuh.house_keeping_service_be.services.ServiceService.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
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
}