package com.TravelMedicineAdvisory.Server.domain.admin.affiliate;

import java.math.BigDecimal;
import java.util.List;

public record AdminAffiliateStatsResponse(
        long totalActiveAffiliates,
        long totalPendingApplications,
        BigDecimal totalCommissionPaid,
        BigDecimal totalCommissionPending,
        BigDecimal totalRevenue,
        List<TopAffiliateItem> topAffiliates
) {
    public record TopAffiliateItem(
            Long affiliateProfileId,
            String userName,
            String userEmail,
            String referralCode,
            BigDecimal totalCommissionEarned
    ) {}
}
