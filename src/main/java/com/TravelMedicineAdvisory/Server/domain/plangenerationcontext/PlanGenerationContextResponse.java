package com.TravelMedicineAdvisory.Server.domain.plangenerationcontext;

import java.time.LocalDateTime;

public record PlanGenerationContextResponse(
        Long id,
        String title,
        String sourceType,
        String fileName,
        String contentType,
        String storagePath,
        String synthesizedText,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
