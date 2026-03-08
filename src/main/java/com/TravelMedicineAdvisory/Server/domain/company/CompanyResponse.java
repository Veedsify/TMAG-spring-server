package com.TravelMedicineAdvisory.Server.domain.company;

import java.time.LocalDateTime;

public record CompanyResponse(
    Long id,
    String name,
    String industry,
    Integer totalCredits,
    Integer usedCredits,
    Integer employeeCount,
    String plan,
    String companyCode,
    Long logoId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
