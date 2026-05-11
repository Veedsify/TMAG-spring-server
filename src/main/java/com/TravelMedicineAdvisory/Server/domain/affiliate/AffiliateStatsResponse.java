package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record AffiliateStatsResponse(
        Integer clicks,
        Integer conversions,
        @JsonProperty("conversion_rate") BigDecimal conversionRate,
        @JsonProperty("total_commission") BigDecimal totalCommission,
        @JsonProperty("pending_commission") BigDecimal pendingCommission,
        @JsonProperty("paid_commission") BigDecimal paidCommission,
        @JsonProperty("total_commission_ngn") BigDecimal totalCommissionNgn,
        @JsonProperty("pending_commission_ngn") BigDecimal pendingCommissionNgn,
        @JsonProperty("paid_commission_ngn") BigDecimal paidCommissionNgn,
        @JsonProperty("active_links") Long activeLinks,
        @JsonProperty("top_campaigns") List<TopCampaignResponse> topCampaigns,
        @JsonProperty("recent_commissions") List<CommissionRecordResponse> recentCommissions,
        @JsonProperty("clicks_chart") List<ClicksChartPointResponse> clicksChart
) {
    public record TopCampaignResponse(
            String campaign,
            Integer clicks,
            Integer conversions,
            BigDecimal commission
    ) {}

    public record ClicksChartPointResponse(
            String date,
            Integer clicks,
            Integer conversions
    ) {}
}
