package com.TravelMedicineAdvisory.Server.domain.doctor;

public record GeneratedPlanSnapshot(
    String status,
    String planJson,
    String provider,
    String modelUsed,
    Integer tokensUsed,
    Long processingTimeMs,
    String errorMessage,
    String signedPdfUrl,
    Boolean isSigned
) {}
