package com.TravelMedicineAdvisory.Server.domain.translation;

public record TranslationRequest(
    String key,
    String value,
    String model,
    Long modelId,
    String language
) {}
