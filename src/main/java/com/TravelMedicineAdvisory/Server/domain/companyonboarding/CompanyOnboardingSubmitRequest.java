package com.TravelMedicineAdvisory.Server.domain.companyonboarding;

import java.util.List;

public record CompanyOnboardingSubmitRequest(
        String companyName,
        String industry,
        String contactEmail,
        String contactPhone,
        String website,
        String billingCurrency,
        String selectedPlanCode,
        Integer creditCount,
        String sampleRequest,
        List<TeamMemberRequest> teamMembers,
        List<PlatformEmployeeRequest> platformEmployees) {

    public record TeamMemberRequest(
            String firstName,
            String lastName,
            String email,
            String role) {
    }

    public record PlatformEmployeeRequest(
            String email,
            String firstName,
            String lastName) {
    }
}
