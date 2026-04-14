package com.TravelMedicineAdvisory.Server.core.seeder;

import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanCode;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(1)
public class CreditPlanSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CreditPlanSeeder.class);

    private final CreditPlanRepository repository;

    public CreditPlanSeeder(CreditPlanRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        seedCreditPlans();
    }

    @Transactional
    protected void seedCreditPlans() {
        if (repository.count() > 0) return;

        logger.info("Seeding user credit plans...");

        List<CreditPlan> plans = List.of(
                createPlan(
                        CreditPlanCode.ESSENTIAL,
                        "Essential",
                        BigDecimal.ZERO,
                        "Generic travel health education report for your destination. No personalisation.",
                        false
                ),
                createPlan(
                        CreditPlanCode.STANDARD,
                        "Standard",
                        new BigDecimal("50.00"),
                        "Fully personalised travel health report using all questionnaire inputs across 14 decision trees.",
                        true
                ),
                createPlan(
                        CreditPlanCode.PREMIUM,
                        "Premium",
                        new BigDecimal("100.00"),
                        "Everything in Standard plus a Pre-Travel Checklist, Medication Packing List, and Doctor-Ready Clinical Summary Letter.",
                        false
                )
        );

        repository.saveAll(plans);
        logger.info("Seeded {} user credit plan entries.", plans.size());
    }

    private CreditPlan createPlan(CreditPlanCode code, String displayName,
                                      BigDecimal basePriceUsd, String description,
                                      boolean isDefault) {
        CreditPlan plan = new CreditPlan();
        plan.setCode(code);
        plan.setDisplayName(displayName);
        plan.setBasePriceUsd(basePriceUsd);
        plan.setDescription(description);
        plan.setIsDefault(isDefault);
        return plan;
    }
}
