package com.TravelMedicineAdvisory.Server.core.ai;

public record AiCallOptions(
        String providerOverride,
        String modelOverride,
        int maxOutputTokens,
        double temperature) {

    public AiCallOptions {
        if (maxOutputTokens <= 0) maxOutputTokens = 4096;
        if (temperature <= 0) temperature = 0.3;
    }
}
