package com.TravelMedicineAdvisory.Server.domain.companyadmin.management;

public record CompanyAdminUserCreateRequest(
        Long companyId,
        String firstName,
        String lastName,
        String email,
        String password,
        String role,
        String department,
        Integer creditsAllocated) {
}
