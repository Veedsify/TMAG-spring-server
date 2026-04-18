package com.TravelMedicineAdvisory.Server.domain.credit;



public record CreditRequest(
    Integer amount,
    String type,
    String reference,
    Integer balanceAfter,
    Long companyId,
    Long userId
) {}
