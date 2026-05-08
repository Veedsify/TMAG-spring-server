package com.TravelMedicineAdvisory.Server.domain.affiliate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AffiliateApplicationRequest(
        @NotBlank String fullName,
        String companyName,
        @NotBlank @Email String email,
        String phone,
        String websiteUrl,
        String socialMediaLinks,
        String estimatedMonthlyReach,
        @NotBlank String promoDescription,
        @NotNull Boolean agreedToTerms
) {}
