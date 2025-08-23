package iuh.house_keeping_service_be.dtos.Admin.UserPermission.response;

import java.util.List;

public record ModulePermissionData(
    String moduleName,
    List<FeaturePermissionData> features
) {}
