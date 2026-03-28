package com.TravelMedicineAdvisory.Server.domain.report;

public record ComplianceAuditDto(
    Long ledgerId,
    String action,
    String employeeName,
    String planDestination,
    String ipAddress,
    String userAgent,
    String timestamp
) {}
