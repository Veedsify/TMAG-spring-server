package com.TravelMedicineAdvisory.Server.domain.permission;

import java.time.LocalDateTime;

public record PermissionResponse(
    Long id,
    String name,
    String description,
    String resourceType,
    String action,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
