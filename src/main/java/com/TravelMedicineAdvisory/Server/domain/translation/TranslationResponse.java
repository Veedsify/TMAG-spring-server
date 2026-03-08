package com.TravelMedicineAdvisory.Server.domain.translation;

import java.time.LocalDateTime;

public record TranslationResponse(
    Long id,
    String key,
    String value,
    String model,
    Long modelId,
    String language,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
