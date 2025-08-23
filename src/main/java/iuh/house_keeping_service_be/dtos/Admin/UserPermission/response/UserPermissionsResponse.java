package iuh.house_keeping_service_be.dtos.Admin.UserPermission.response;

import java.util.List;

public record UserPermissionsResponse(
    boolean success,
    String message,
    UserPermissionData data
) {}

