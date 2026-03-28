package com.TravelMedicineAdvisory.Server.domain.creditrequest;

import java.time.LocalDateTime;

public record CreditRequestResponse(
    Long id,
    Integer creditsRequested,
    String reason,
    String status,
    LocalDateTime submittedAt,
    Long companyId,
    Long employeeId,
    String employeeName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
