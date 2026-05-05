package com.TravelMedicineAdvisory.Server.domain.travelplan;

/**
 * Snapshot of {@link com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan} for API responses.
 * Included on {@link TravelPlanResponse} when fetching a single plan by id.
 */
public record GeneratedPlanPayload(
        String status,
        String planJson,
        String provider,
        String modelUsed,
        Integer tokensUsed,
        Long processingTimeMs,
        String errorMessage,
        String signedPdfUrl,
        String summaryPdfUrl,
        Boolean isSigned
) {}
