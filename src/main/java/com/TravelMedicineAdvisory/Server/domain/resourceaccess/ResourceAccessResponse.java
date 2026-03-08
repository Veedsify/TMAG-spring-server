package com.TravelMedicineAdvisory.Server.domain.resourceaccess;

import java.time.LocalDateTime;

public record ResourceAccessResponse(
    Long id,
    String roleId,
    Long memberId,
    String resourceType,
    String resourceId,
    String accessType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
