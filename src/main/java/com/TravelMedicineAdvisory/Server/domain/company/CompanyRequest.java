package com.TravelMedicineAdvisory.Server.domain.company;

public record CompanyRequest(
    String name,
    String industry,
    Integer totalCredits,
    Integer usedCredits,
    Integer employeeCount,
    String plan,
    String companyCode,
    Long logoId
) {}
