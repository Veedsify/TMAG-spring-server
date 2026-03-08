package com.TravelMedicineAdvisory.Server.domain.credit;

import java.math.BigDecimal;

public record CreditRequest(
    BigDecimal amount,
    String type,
    String reference,
    BigDecimal balanceAfter,
    Long companyId,
    Long userId
) {}
