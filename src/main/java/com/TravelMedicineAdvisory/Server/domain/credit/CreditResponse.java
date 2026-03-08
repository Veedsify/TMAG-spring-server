package com.TravelMedicineAdvisory.Server.domain.credit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditResponse(
    Long id,
    BigDecimal amount,
    String type,
    String reference,
    BigDecimal balanceAfter,
    Long companyId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
