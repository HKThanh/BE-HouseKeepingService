package iuh.house_keeping_service_be.dtos.Admin.UserPermission.response;

import java.util.List;

public record PermissionManagementResponse(
    boolean success,
    String message,
    List<RolePermissionData> data
) {}

