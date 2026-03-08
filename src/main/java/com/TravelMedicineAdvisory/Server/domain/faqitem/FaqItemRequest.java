package com.TravelMedicineAdvisory.Server.domain.faqitem;

public record FaqItemRequest(
    String question,
    String answer,
    String category,
    Integer position,
    Boolean isActive
) {}
