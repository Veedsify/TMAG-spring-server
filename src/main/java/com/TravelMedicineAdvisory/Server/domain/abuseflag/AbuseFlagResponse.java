package com.TravelMedicineAdvisory.Server.domain.abuseflag;

import java.time.LocalDateTime;

public record AbuseFlagResponse(
    Long id,
    String type,
    String description,
    String severity,
    Boolean resolved,
    Long resolvedBy,
    LocalDateTime resolvedAt,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
