package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AffiliateApplicationRequest(
        @NotBlank @JsonProperty("fullName") String fullName,
        @JsonProperty("companyName") String companyName,
        @NotBlank @Email String email,
        String phone,
        @JsonProperty("websiteUrl") String websiteUrl,
        @JsonProperty("socialMediaLinks") String socialMediaLinks,
        @JsonProperty("estimatedMonthlyReach") String estimatedMonthlyReach,
        @NotBlank @JsonProperty("promoDescription") String promoDescription,
        @NotNull @JsonProperty("agreedToTerms") Boolean agreedToTerms
) {}
