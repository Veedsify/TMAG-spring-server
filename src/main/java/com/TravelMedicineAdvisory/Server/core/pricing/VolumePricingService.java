package com.TravelMedicineAdvisory.Server.core.pricing;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VolumePricingService {

    private static final int TIER_1_MAX = 49;
    private static final int TIER_2_MAX = 99;
    private static final int TIER_3_MAX = 499;

    public record TierPrice(
            BigDecimal pricePerCredit,
            String appliedTier,
            boolean contactSales
    ) {}

    public record FullPricing(
            BigDecimal standardTier1Usd,
            BigDecimal standardTier2Usd,
            BigDecimal standardTier3Usd,
            BigDecimal premiumTier1Usd,
            BigDecimal premiumTier2Usd,
            BigDecimal premiumTier3Usd,
            BigDecimal standardTier1Ngn,
            BigDecimal standardTier2Ngn,
            BigDecimal standardTier3Ngn,
            BigDecimal premiumTier1Ngn,
            BigDecimal premiumTier2Ngn,
            BigDecimal premiumTier3Ngn,
            int tier1MaxCredits,
            int tier2MaxCredits,
            int tier3MaxCredits
    ) {}

    public record PublicPricingPreview(
            String serviceLevel,
            BillingCurrency currency,
            int credits,
            BigDecimal pricePerCredit,
            BigDecimal totalAmount,
            String appliedTier,
            boolean contactSales
    ) {}

    public TierPrice computePrice(int credits, String serviceLevel, BillingCurrency currency) {
        if (credits >= 500) {
            return new TierPrice(BigDecimal.ZERO, "TIER_3", true);
        }

        boolean isPremium = "PREMIUM".equalsIgnoreCase(serviceLevel);
        BigDecimal priceUsd;

        if (credits >= 100) {
            priceUsd = isPremium ? new BigDecimal("80") : new BigDecimal("40");
            return new TierPrice(convert(priceUsd, currency), "TIER_3", false);
        } else if (credits >= 50) {
            priceUsd = isPremium ? new BigDecimal("90") : new BigDecimal("45");
            return new TierPrice(convert(priceUsd, currency), "TIER_2", false);
        } else {
            priceUsd = isPremium ? new BigDecimal("100") : new BigDecimal("50");
            return new TierPrice(convert(priceUsd, currency), "TIER_1", false);
        }
    }

    public FullPricing getFullPricing() {
        return new FullPricing(
                new BigDecimal("50"), new BigDecimal("45"), new BigDecimal("40"),
                new BigDecimal("100"), new BigDecimal("90"), new BigDecimal("80"),
                new BigDecimal("50000"), new BigDecimal("45000"), new BigDecimal("40000"),
                new BigDecimal("100000"), new BigDecimal("90000"), new BigDecimal("80000"),
                TIER_1_MAX, TIER_2_MAX, TIER_3_MAX
        );
    }

    public List<PublicPricingPreview> getPublicPricingPreviews(int credits) {
        return List.of(
                buildPreview("STANDARD", BillingCurrency.USD, credits),
                buildPreview("STANDARD", BillingCurrency.NGN, credits),
                buildPreview("PREMIUM", BillingCurrency.USD, credits),
                buildPreview("PREMIUM", BillingCurrency.NGN, credits)
        );
    }

    private PublicPricingPreview buildPreview(String serviceLevel, BillingCurrency currency, int credits) {
        TierPrice tier = computePrice(credits, serviceLevel, currency);
        BigDecimal total = tier.contactSales()
                ? BigDecimal.ZERO
                : tier.pricePerCredit().multiply(BigDecimal.valueOf(credits));
        return new PublicPricingPreview(
                serviceLevel, currency, credits,
                tier.pricePerCredit(), total,
                tier.appliedTier(), tier.contactSales()
        );
    }

    private BigDecimal convert(BigDecimal priceUsd, BillingCurrency currency) {
        if (currency == BillingCurrency.NGN) {
            return switch (priceUsd.intValue()) {
                case 40 -> new BigDecimal("40000");
                case 45 -> new BigDecimal("45000");
                case 80 -> new BigDecimal("80000");
                case 90 -> new BigDecimal("90000");
                case 100 -> new BigDecimal("100000");
                default -> new BigDecimal("50000");
            };
        }
        return priceUsd;
    }
}
