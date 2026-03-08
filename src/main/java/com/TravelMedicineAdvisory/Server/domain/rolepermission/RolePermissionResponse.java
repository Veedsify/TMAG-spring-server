package com.TravelMedicineAdvisory.Server.domain.rolepermission;

import java.time.LocalDateTime;

public record RolePermissionResponse(
    Long id,
    Long roleId,
    Long permissionId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
