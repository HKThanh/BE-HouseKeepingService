package iuh.house_keeping_service_be.dtos.Admin.UserPermission.request;

public record UpdatePermissionRequest(
    Integer roleId,
    Integer featureId,
    Boolean isEnabled
) {}