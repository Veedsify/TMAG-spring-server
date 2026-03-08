package com.TravelMedicineAdvisory.Server.domain.systemlog;

public record SystemLogRequest(
    String level,
    String message,
    String source,
    String details
) {}
