package com.TravelMedicineAdvisory.Server.domain.draftplan;

public record SaveDraftPlanRequest(
    String country,
    String answersJson,
    Integer categoryIndex,
    Boolean showVerify,
    Boolean showIntro,
    Boolean riskConsentGiven
) {}
