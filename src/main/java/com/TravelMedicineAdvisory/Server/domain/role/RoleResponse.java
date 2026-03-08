package com.TravelMedicineAdvisory.Server.domain.role;

import java.time.LocalDateTime;

public record RoleResponse(
    Long id,
    String name,
    String permissions,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
