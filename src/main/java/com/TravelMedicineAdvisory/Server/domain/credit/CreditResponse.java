package com.TravelMedicineAdvisory.Server.domain.credit;


import java.time.LocalDateTime;

public record CreditResponse(
    Long id,
    Integer amount,
    String type,
    String reference,
    Integer balanceAfter,
    Long companyId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
