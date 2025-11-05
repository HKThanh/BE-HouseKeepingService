package iuh.house_keeping_service_be.services.AdminService;

import iuh.house_keeping_service_be.dtos.Statistics.RevenueStatisticsResponse;
import iuh.house_keeping_service_be.dtos.Statistics.ServiceBookingStatisticsResponse;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.AdminProfile;

import java.time.LocalDate;

public interface AdminService {
    AdminProfile findByAccountId(String accountId);

    AdminProfile findByContactInfoAndAccountRole(String contactInfo, RoleName roleName);

    AdminProfile findById(String id);

    boolean isAdminByUsername(String username);

    ServiceBookingStatisticsResponse getServiceBookingStatistics(String period, LocalDate startDate, LocalDate endDate);

    RevenueStatisticsResponse getRevenueStatistics(String period, LocalDate startDate, LocalDate endDate);
}
