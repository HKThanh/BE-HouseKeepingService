package iuh.house_keeping_service_be.services.AdminService;

import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.PermissionManagementResponse;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.RoleListResponse;
import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.UserPermissionsResponse;
import iuh.house_keeping_service_be.enums.RoleName;

import java.util.List;

public interface PermissionService {

    /**
     * Lấy danh sách tất cả vai trò (trừ ADMIN) để hiển thị cho Admin chọn
     */
    RoleListResponse getAllManageableRoles();

    /**
     * Lấy tất cả quyền của một vai trò cụ thể để hiển thị trên giao diện quản lý
     */
    PermissionManagementResponse getRolePermissions(Integer roleId);

    /**
     * Cập nhật trạng thái một quyền cụ thể của một vai trò
     */
    PermissionManagementResponse updateRolePermission(Integer roleId, Integer featureId, Boolean isEnabled);

    /**
     * Lấy danh sách quyền được kích hoạt của người dùng hiện tại
     */
    UserPermissionsResponse getUserPermissions(String username);

    /**
     * Lấy danh sách quyền của một vai trò cụ thể (dành cho việc kiểm tra quyền)
     */
    List<String> getEnabledPermissionsByRole(RoleName roleName);

    /**
     * Kiểm tra xem một người dùng có quyền thực hiện một chức năng cụ thể không
     */
    boolean hasPermission(String username, String featureName);
}