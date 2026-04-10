package com.TravelMedicineAdvisory.Server.domain.companyadmin.management;

public record CompanyAdminUserUpdateRequest(
        String firstName,
        String lastName,
        String email,
        String role,
        String department,
        String employeeStatus,
        Integer creditsAllocated) {
}
