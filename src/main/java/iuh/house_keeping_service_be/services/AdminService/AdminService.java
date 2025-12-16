package iuh.house_keeping_service_be.services.AdminService;

import iuh.house_keeping_service_be.dtos.Admin.response.AdminProfileResponse;
import iuh.house_keeping_service_be.dtos.Admin.response.UserAccountResponse;
import iuh.house_keeping_service_be.dtos.Statistics.RevenueStatisticsResponse;
import iuh.house_keeping_service_be.dtos.Statistics.ServiceBookingStatisticsResponse;
import iuh.house_keeping_service_be.dtos.Admin.request.UpdateAccountStatusRequest;
import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.enums.UserType;
import iuh.house_keeping_service_be.models.AdminProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AdminService {
    AdminProfile findByAccountId(String accountId);

    AdminProfile findByContactInfoAndAccountRole(String contactInfo, RoleName roleName);

    AdminProfile findById(String id);
    
    AdminProfileResponse getAdminProfile(String adminProfileId);

    boolean isAdminByUsername(String username);

    ServiceBookingStatisticsResponse getServiceBookingStatistics(String period, LocalDate startDate, LocalDate endDate);

    RevenueStatisticsResponse getRevenueStatistics(String period, LocalDate startDate, LocalDate endDate);

    Page<UserAccountResponse> getUserAccounts(UserType userType, AccountStatus status, Pageable pageable);

    UserAccountResponse updateAccountStatus(String accountId, UpdateAccountStatusRequest request);
}
