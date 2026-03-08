package com.TravelMedicineAdvisory.Server.domain.resourcepermission;

public record ResourcePermissionRequest(
    String resourceType,
    String resourceId,
    Long userId,
    String action,
    String defaultScope,
    Long roleId,
    Long permissionId
) {}
