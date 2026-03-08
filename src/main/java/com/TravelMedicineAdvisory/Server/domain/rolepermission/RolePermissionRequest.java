package com.TravelMedicineAdvisory.Server.domain.rolepermission;

public record RolePermissionRequest(
    Long roleId,
    Long permissionId
) {}
