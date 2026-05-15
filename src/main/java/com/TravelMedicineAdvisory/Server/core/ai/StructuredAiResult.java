package com.TravelMedicineAdvisory.Server.core.ai;

public record StructuredAiResult<T>(
        T value,
        String rawJson,
        String provider,
        String model,
        Integer estimatedTokens) {
}
