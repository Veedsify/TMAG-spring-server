package com.TravelMedicineAdvisory.Server.domain.admin.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record AdminAffiliateStatsResponse(
        long totalActiveAffiliates,
        long totalPendingApplications,
        BigDecimal totalCommissionPaid,
        BigDecimal totalCommissionPending,
        BigDecimal totalRevenue,
        @JsonProperty("conversionRate") BigDecimal conversionRate,
        @JsonProperty("totalClicks") long totalClicks,
        @JsonProperty("totalConversions") long totalConversions,
        @JsonProperty("clicksChart") List<ClicksChartPoint> clicksChart,
        List<TopAffiliateItem> topAffiliates
) {
    public record TopAffiliateItem(
            Long affiliateProfileId,
            String userName,
            String userEmail,
            String referralCode,
            BigDecimal totalCommissionEarned
    ) {}

    public record ClicksChartPoint(
            String date,
            long clicks,
            long conversions
    ) {}
}
