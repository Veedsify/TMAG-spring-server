package com.TravelMedicineAdvisory.Server.domain.systemlog;

import java.time.LocalDateTime;

public record SystemLogResponse(
    Long id,
    String level,
    String message,
    String source,
    String details,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
