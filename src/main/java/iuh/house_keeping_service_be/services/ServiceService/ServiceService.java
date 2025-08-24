package iuh.house_keeping_service_be.services.ServiceService;

import iuh.house_keeping_service_be.dtos.Service.ServiceDetailResponse;
import iuh.house_keeping_service_be.dtos.Service.ServiceListResponse;

public interface ServiceService {

    /**
     * Lấy danh sách tất cả các dịch vụ đang hoạt động
     */
    ServiceListResponse getAllActiveServices();

    /**
     * Lấy chi tiết một dịch vụ cụ thể
     */
    ServiceDetailResponse getServiceDetail(Integer serviceId);

    /**
     * Tìm kiếm dịch vụ theo tên
     */
    ServiceListResponse searchServices(String keyword);

    /**
     * Lấy số lượng dịch vụ đang hoạt động
     */
    Long countActiveServices();
}