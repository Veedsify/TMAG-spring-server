package com.TravelMedicineAdvisory.Server.domain.permission;

public record PermissionRequest(
    String name,
    String description,
    String resourceType,
    String action
) {}
