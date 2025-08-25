package iuh.house_keeping_service_be.services.ServiceCategoryService;

import iuh.house_keeping_service_be.dtos.ServiceCategory.CategoryWithServicesResponse;
import iuh.house_keeping_service_be.dtos.ServiceCategory.ServiceCategoryListResponse;

public interface ServiceCategoryService {

    /**
     * Lấy danh sách tất cả danh mục dịch vụ đang hoạt động
     */
    ServiceCategoryListResponse getAllActiveCategories();

    /**
     * Lấy danh mục cùng với các dịch vụ trong danh mục đó
     */
    CategoryWithServicesResponse getCategoryWithServices(Integer categoryId);

    /**
     * Đếm số lượng dịch vụ trong một danh mục
     */
    Long countServicesByCategory(Integer categoryId);
}