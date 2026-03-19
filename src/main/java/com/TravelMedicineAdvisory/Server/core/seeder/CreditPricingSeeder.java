package com.TravelMedicineAdvisory.Server.core.seeder;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.creditpricing.CreditPricing;
import com.TravelMedicineAdvisory.Server.domain.creditpricing.CreditPricingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class CreditPricingSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CreditPricingSeeder.class);

    private final CreditPricingRepository repository;

    public CreditPricingSeeder(CreditPricingRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        seedCreditPricing();
    }

    @Transactional
    protected void seedCreditPricing() {
        if (repository.count() > 0) return;
        logger.info("Seeding credit pricing with discount tiers...");

        // Base NGN pricing: 5000 NGN per credit
        // Discount Tier 1: 5000 NGN off when total >= 50000 NGN (10+ credits)
        // Discount Tier 2: 8500 NGN off when total >= 100000 NGN (20+ credits)
        // Discount Tier 3: 20000 NGN off when total >= 500000 NGN (100+ credits)

        BigDecimal ngnPricePerCredit = new BigDecimal("5000.00");
        BigDecimal ngnDiscountTier1Threshold = new BigDecimal("50000.00");
        BigDecimal ngnDiscountTier1Amount = new BigDecimal("5000.00");
        BigDecimal ngnDiscountTier2Threshold = new BigDecimal("100000.00");
        BigDecimal ngnDiscountTier2Amount = new BigDecimal("8500.00");
        BigDecimal ngnDiscountTier3Threshold = new BigDecimal("500000.00");
        BigDecimal ngnDiscountTier3Amount = new BigDecimal("20000.00");

        List<CreditPricing> pricing = List.of(
            createPricingWithDiscounts(
                BillingCurrency.NGN, "₦", ngnPricePerCredit,
                ngnDiscountTier1Threshold, ngnDiscountTier1Amount,
                ngnDiscountTier2Threshold, ngnDiscountTier2Amount,
                ngnDiscountTier3Threshold, ngnDiscountTier3Amount,
                1, 100, 1
            ),
            createPricingWithDiscounts(
                BillingCurrency.USD, "$",
                ngnPricePerCredit.divide(new BigDecimal("1550"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier1Threshold.divide(new BigDecimal("1550"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier1Amount.divide(new BigDecimal("1550"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier2Threshold.divide(new BigDecimal("1550"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier2Amount.divide(new BigDecimal("1550"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier3Threshold.divide(new BigDecimal("1550"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier3Amount.divide(new BigDecimal("1550"), 2, RoundingMode.HALF_UP),
                1, 100, 2
            ),
            createPricingWithDiscounts(
                BillingCurrency.EUR, "€",
                ngnPricePerCredit.divide(new BigDecimal("1700"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier1Threshold.divide(new BigDecimal("1700"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier1Amount.divide(new BigDecimal("1700"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier2Threshold.divide(new BigDecimal("1700"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier2Amount.divide(new BigDecimal("1700"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier3Threshold.divide(new BigDecimal("1700"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier3Amount.divide(new BigDecimal("1700"), 2, RoundingMode.HALF_UP),
                1, 100, 3
            ),
            createPricingWithDiscounts(
                BillingCurrency.GBP, "£",
                ngnPricePerCredit.divide(new BigDecimal("1950"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier1Threshold.divide(new BigDecimal("1950"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier1Amount.divide(new BigDecimal("1950"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier2Threshold.divide(new BigDecimal("1950"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier2Amount.divide(new BigDecimal("1950"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier3Threshold.divide(new BigDecimal("1950"), 2, RoundingMode.HALF_UP),
                ngnDiscountTier3Amount.divide(new BigDecimal("1950"), 2, RoundingMode.HALF_UP),
                1, 100, 4
            )
        );

        repository.saveAll(pricing);
        logger.info("Seeded {} credit pricing entries with discount tiers.", pricing.size());
    }

    private CreditPricing createPricingWithDiscounts(
            BillingCurrency currency, String symbol, BigDecimal price,
            BigDecimal tier1Threshold, BigDecimal tier1Amount,
            BigDecimal tier2Threshold, BigDecimal tier2Amount,
            BigDecimal tier3Threshold, BigDecimal tier3Amount,
            int min, int max, int order) {
        CreditPricing cp = new CreditPricing();
        cp.setCurrency(currency);
        cp.setCurrencySymbol(symbol);
        cp.setPricePerCredit(price);
        cp.setMinCredits(min);
        cp.setMaxCredits(max);
        cp.setDiscountTier1Threshold(tier1Threshold);
        cp.setDiscountTier1Amount(tier1Amount);
        cp.setDiscountTier2Threshold(tier2Threshold);
        cp.setDiscountTier2Amount(tier2Amount);
        cp.setDiscountTier3Threshold(tier3Threshold);
        cp.setDiscountTier3Amount(tier3Amount);
        cp.setActive(true);
        cp.setDisplayOrder(order);
        return cp;
    }
}
