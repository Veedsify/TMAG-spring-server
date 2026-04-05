package com.TravelMedicineAdvisory.Server.core.ai;

public record AiGenerationResult(
        String content,
        String provider,
        String model,
        Integer estimatedTokens
) {
}
