package com.TravelMedicineAdvisory.Server.domain.report;

import java.time.LocalDateTime;

public record ComplianceAuditProjection(
        Long ledgerId,
        String action,
        String employeeName,
        String fallbackUserName,
        String planDestination,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt) {
}
