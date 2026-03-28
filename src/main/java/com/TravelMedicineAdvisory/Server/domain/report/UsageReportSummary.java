package com.TravelMedicineAdvisory.Server.domain.report;

public record UsageReportSummary(
    int totalEmployees,
    int totalPlansGenerated,
    int totalCreditsUsed,
    int totalCreditsAllocated,
    java.util.List<UsageReportDto> employees
) {}
