package com.TravelMedicineAdvisory.Server.domain.admin.affiliate;

import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateReferral;

import java.time.format.DateTimeFormatter;

public record AdminAffiliateReferralResponse(
        Long id,
        String referredUserEmail,
        String referredUserName,
        String referralCode,
        String status,
        String firstClickAt,
        String convertedAt,
        String createdAt
) {
    public static AdminAffiliateReferralResponse from(AffiliateReferral r) {
        String userEmail = null;
        String userName = null;
        if (r.getReferredUser() != null) {
            userEmail = r.getReferredUser().getEmail();
            userName = r.getReferredUser().getFirstName() != null
                    ? r.getReferredUser().getFirstName() + (r.getReferredUser().getLastName() != null ? " " + r.getReferredUser().getLastName() : "")
                    : r.getReferredUser().getEmail();
        }
        return new AdminAffiliateReferralResponse(
                r.getId(),
                userEmail,
                userName,
                r.getReferralCode(),
                r.getStatus(),
                r.getFirstClickAt() != null ? r.getFirstClickAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                r.getConvertedAt() != null ? r.getConvertedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );
    }
}
