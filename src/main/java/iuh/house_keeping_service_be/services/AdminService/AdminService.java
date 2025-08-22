package iuh.house_keeping_service_be.services.AdminService;

import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.AdminProfile;

public interface AdminService {
    AdminProfile findByAccountId(String accountId);

    AdminProfile findByContactInfoAndAccountRole(String contactInfo, RoleName roleName);

    AdminProfile findById(String id);
}
