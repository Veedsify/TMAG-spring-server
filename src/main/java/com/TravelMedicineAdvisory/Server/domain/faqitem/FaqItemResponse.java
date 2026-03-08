package com.TravelMedicineAdvisory.Server.domain.faqitem;

import java.time.LocalDateTime;

public record FaqItemResponse(
    Long id,
    String question,
    String answer,
    String category,
    Integer position,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
