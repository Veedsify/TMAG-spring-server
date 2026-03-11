package com.TravelMedicineAdvisory.Server.domain.travelrequest;

import java.time.LocalDateTime;

public record TravelRequestRequest(
    String destination,
    String dates,
    String status,
    LocalDateTime submittedAt,
    Long companyId,
    Long employeeId
) {}
