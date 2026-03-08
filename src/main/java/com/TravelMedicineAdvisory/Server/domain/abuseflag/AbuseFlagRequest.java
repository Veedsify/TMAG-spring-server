package com.TravelMedicineAdvisory.Server.domain.abuseflag;

import java.time.LocalDateTime;

public record AbuseFlagRequest(
    String type,
    String description,
    String severity,
    Boolean resolved,
    Long resolvedBy,
    LocalDateTime resolvedAt,
    Long userId
) {}
