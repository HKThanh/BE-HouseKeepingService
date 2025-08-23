package iuh.house_keeping_service_be.dtos.Admin.UserPermission.response;

import java.util.List;

public record RolePermissionData(
    Integer roleId,
    String roleName,
    List<ModulePermissionData> modules
) {}
