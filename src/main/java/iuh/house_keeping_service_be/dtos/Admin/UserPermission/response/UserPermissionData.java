package iuh.house_keeping_service_be.dtos.Admin.UserPermission.response;

import java.util.List;

public record UserPermissionData(
    String username,
    String role,
    List<String> permissions
) {}
