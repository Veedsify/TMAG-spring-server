package com.TravelMedicineAdvisory.Server.domain.airequestlog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AiRequestLogResponse(
    Long id,
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
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
