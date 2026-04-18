package com.TravelMedicineAdvisory.Server.domain.draftplan;

import java.time.LocalDateTime;

public record DraftPlanResponse(
    Long id,
    String title,
    String country,
    String answersJson,
    Integer categoryIndex,
    Boolean showVerify,
    Boolean showIntro,
    Boolean riskConsentGiven,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
