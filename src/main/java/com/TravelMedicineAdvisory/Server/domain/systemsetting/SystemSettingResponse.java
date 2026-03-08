package com.TravelMedicineAdvisory.Server.domain.systemsetting;

import java.time.LocalDateTime;

public record SystemSettingResponse(
    Long id,
    String key,
    String value,
    String type,
    String group,
    String label,
    String description,
    Boolean isPublic,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
