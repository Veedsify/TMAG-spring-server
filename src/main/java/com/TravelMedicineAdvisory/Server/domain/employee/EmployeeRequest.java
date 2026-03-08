package com.TravelMedicineAdvisory.Server.domain.employee;

public record EmployeeRequest(
    String name,
    String email,
    String department,
    Integer creditsUsed,
    Integer creditsAllocated,
    String status,
    Integer plansGenerated,
    Long companyId,
    Long userId
) {}
