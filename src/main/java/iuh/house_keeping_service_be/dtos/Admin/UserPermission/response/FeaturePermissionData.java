package iuh.house_keeping_service_be.dtos.Admin.UserPermission.response;

public record FeaturePermissionData(
    Integer featureId,
    String featureName,
    String description,
    Boolean isEnabled
) {}
