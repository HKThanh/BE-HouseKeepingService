package iuh.house_keeping_service_be.services.ServiceCategoryService.impl;

import iuh.house_keeping_service_be.dtos.Service.ServiceData;
import iuh.house_keeping_service_be.dtos.ServiceCategory.*;
import iuh.house_keeping_service_be.models.Service;
import iuh.house_keeping_service_be.models.ServiceCategory;
import iuh.house_keeping_service_be.repositories.ServiceCategoryRepository;
import iuh.house_keeping_service_be.repositories.ServiceRepository;
import iuh.house_keeping_service_be.services.ServiceCategoryService.ServiceCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceRepository serviceRepository;

    @Override
    @Transactional(readOnly = true)
    public ServiceCategoryListResponse getAllActiveCategories() {
        try {
            List<ServiceCategory> categories = serviceCategoryRepository.findAllActiveCategories();

            List<ServiceCategoryData> categoryDataList = categories.stream()
                    .map(this::convertToCategoryData)
                    .collect(Collectors.toList());

            return new ServiceCategoryListResponse(
                    true,
                    "Lấy danh sách danh mục dịch vụ thành công",
                    categoryDataList
            );

        } catch (Exception e) {
            log.error("Error getting all active categories: {}", e.getMessage());
            return new ServiceCategoryListResponse(
                    false,
                    "Lỗi khi lấy danh sách danh mục dịch vụ",
                    null
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryWithServicesResponse getCategoryWithServices(Integer categoryId) {
        try {
            Optional<ServiceCategory> categoryOpt = serviceCategoryRepository.findActiveCategoryById(categoryId);

            if (categoryOpt.isEmpty()) {
                return new CategoryWithServicesResponse(
                        false,
                        "Không tìm thấy danh mục dịch vụ",
                        null
                );
            }

            ServiceCategory category = categoryOpt.get();
            List<Service> services = serviceRepository.findActiveServicesByCategory(categoryId);

            List<ServiceData> serviceDataList = services.stream()
                    .map(this::convertToServiceData)
                    .collect(Collectors.toList());

            CategoryWithServicesData categoryData = new CategoryWithServicesData(
                    category.getCategoryId(),
                    category.getCategoryName(),
                    category.getDescription(),
                    category.getIconUrl(),
                    serviceDataList
            );

            return new CategoryWithServicesResponse(
                    true,
                    "Lấy thông tin danh mục và dịch vụ thành công",
                    categoryData
            );

        } catch (Exception e) {
            log.error("Error getting category with services for categoryId {}: {}", categoryId, e.getMessage());
            return new CategoryWithServicesResponse(
                    false,
                    "Lỗi khi lấy thông tin danh mục và dịch vụ",
                    null
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countServicesByCategory(Integer categoryId) {
        try {
            return serviceCategoryRepository.countActiveServicesByCategory(categoryId);
        } catch (Exception e) {
            log.error("Error counting services for categoryId {}: {}", categoryId, e.getMessage());
            return 0L;
        }
    }

    private ServiceCategoryData convertToCategoryData(ServiceCategory category) {
        Long serviceCount = countServicesByCategory(category.getCategoryId());

        return new ServiceCategoryData(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getDescription(),
                category.getIconUrl(),
                category.getIsActive(),
                serviceCount.intValue()
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
                service.getRecommendedStaff(),
                service.getIconUrl(),
                service.getIsActive()
        );
    }
}