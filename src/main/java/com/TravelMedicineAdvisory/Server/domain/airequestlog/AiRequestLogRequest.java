package com.TravelMedicineAdvisory.Server.domain.airequestlog;

import java.math.BigDecimal;

public record AiRequestLogRequest(
    String destination,
    String promptSummary,
    String outputSummary,
    Integer tokensUsed,
    Long processingTimeMs,
    String status,
    String errorMessage,
    String riskLevel,
    String modelUsed,
    BigDecimal creditConsumed,
    Long companyId,
    Long userId
) {}
