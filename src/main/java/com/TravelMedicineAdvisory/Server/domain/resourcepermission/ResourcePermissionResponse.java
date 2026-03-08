package com.TravelMedicineAdvisory.Server.domain.resourcepermission;

import java.time.LocalDateTime;

public record ResourcePermissionResponse(
    Long id,
    String resourceType,
    String resourceId,
    Long userId,
    String action,
    String defaultScope,
    Long roleId,
    Long permissionId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
