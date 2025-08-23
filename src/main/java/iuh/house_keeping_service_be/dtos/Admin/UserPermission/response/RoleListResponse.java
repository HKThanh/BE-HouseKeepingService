package iuh.house_keeping_service_be.dtos.Admin.UserPermission.response;

import java.util.List;

public record RoleListResponse(
    boolean success,
    String message,
    List<RoleData> data
) {}

