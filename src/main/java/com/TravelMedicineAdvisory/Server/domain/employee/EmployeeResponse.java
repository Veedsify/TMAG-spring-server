package com.TravelMedicineAdvisory.Server.domain.employee;

import java.time.LocalDateTime;

public record EmployeeResponse(
    Long id,
    String name,
    String email,
    String department,
    String role,
    Integer creditsUsed,
    Integer creditsAllocated,
    String status,
    Integer plansGenerated,
    Long companyId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
