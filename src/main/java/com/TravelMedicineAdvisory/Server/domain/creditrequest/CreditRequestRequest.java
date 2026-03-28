package com.TravelMedicineAdvisory.Server.domain.creditrequest;

import java.time.LocalDateTime;

public record CreditRequestRequest(
    Integer creditsRequested,
    String reason,
    String status,
    LocalDateTime submittedAt,
    Long companyId,
    Long employeeId
) {}
