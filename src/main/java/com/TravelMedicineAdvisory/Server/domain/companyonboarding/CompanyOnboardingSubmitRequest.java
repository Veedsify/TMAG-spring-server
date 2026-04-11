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
        String sampleRequest,
        List<TeamMemberRequest> teamMembers) {

    public record TeamMemberRequest(
            String name,
            String email,
            String role) {
    }
}
