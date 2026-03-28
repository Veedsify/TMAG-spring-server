package com.TravelMedicineAdvisory.Server.domain.report;

import java.util.List;

public record ComplianceReportDto(
    List<ComplianceAuditDto> audits,
    int totalRecords,
    String generatedAt
) {}
