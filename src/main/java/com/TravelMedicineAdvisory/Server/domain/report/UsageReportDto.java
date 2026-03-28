package com.TravelMedicineAdvisory.Server.domain.report;

import java.time.LocalDateTime;

public record UsageReportDto(
    String employeeName,
    String employeeEmail,
    String department,
    Integer creditsAllocated,
    Integer creditsUsed,
    Integer plansGenerated,
    String status,
    LocalDateTime lastActivityAt
) {}
