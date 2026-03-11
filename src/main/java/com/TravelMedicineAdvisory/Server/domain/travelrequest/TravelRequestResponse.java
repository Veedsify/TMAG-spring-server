package com.TravelMedicineAdvisory.Server.domain.travelrequest;

import java.time.LocalDateTime;

public record TravelRequestResponse(
    Long id,
    String destination,
    String dates,
    String status,
    LocalDateTime submittedAt,
    Long companyId,
    Long employeeId,
    String employeeName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
