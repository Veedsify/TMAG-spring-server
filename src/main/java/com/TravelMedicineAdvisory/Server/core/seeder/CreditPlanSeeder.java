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
        logger.info("Seeding user credit plans...");

        List<CreditPlan> plans = List.of(
                // Individual plans
                createPlan(CreditPlanCode.ESSENTIAL, "Essential",
                        BigDecimal.ZERO, BigDecimal.ZERO,
                        "Generic travel health education report for your destination. No personalisation.",
                        true, false, null, null),

                createPlan(CreditPlanCode.STANDARD, "Standard",
                        new BigDecimal("50.00"), new BigDecimal("50000.00"),
                        "Fully personalised travel health report using all questionnaire inputs across 14 decision trees.",
                        false, false, null, null),

                createPlan(CreditPlanCode.PREMIUM, "Premium",
                        new BigDecimal("100.00"), new BigDecimal("100000.00"),
                        "Everything in Standard plus a Pre-Travel Checklist, Medication Packing List, and Doctor-Ready Clinical Summary Letter.",
                        false, false, null, null),

                // Standalone family plans
                createFamilyPlan(CreditPlanCode.FAMILY_ONE_TRIP, "One Family Plan",
                        new BigDecimal("180.00"), new BigDecimal("180000.00"),
                        "One standalone family travel health plan for up to 6 family members. Additional family members are billed separately.",
                        1),

                createFamilyPlan(CreditPlanCode.FAMILY_TWO_TRIP, "Two Family Plans",
                        new BigDecimal("360.00"), new BigDecimal("360000.00"),
                        "Two standalone family travel health plans. Each plan includes up to 6 family members; additional family members are billed separately.",
                        2),

                // Enterprise company plans — 0-100 signups
                createPlan(CreditPlanCode.ENTERPRISE_SILVER, "Enterprise Silver",
                        new BigDecimal("50.00"), new BigDecimal("50000.00"),
                        "Fully personalised travel health report across 14 clinical decision trees. Ideal for teams of up to 100.",
                        false, true, "0-100", "STANDARD"),

                createPlan(CreditPlanCode.ENTERPRISE_PLUS, "Enterprise Plus",
                        new BigDecimal("100.00"), new BigDecimal("100000.00"),
                        "Everything in Standard plus Pre-Travel Checklist, Medication Packing List, and Doctor-Ready Clinical Summary Letter. For teams up to 100.",
                        false, true, "0-100", "PREMIUM"),

                // Enterprise company plans — 100-500 signups
                createPlan(CreditPlanCode.ENTERPRISE_GOLD, "Enterprise Gold",
                        new BigDecimal("50.00"), new BigDecimal("50000.00"),
                        "Fully personalised travel health report across 14 clinical decision trees. Built for mid-size teams of 100–500.",
                        false, true, "100-500", "STANDARD"),

                createPlan(CreditPlanCode.ENTERPRISE_ELITE, "Enterprise Elite",
                        new BigDecimal("100.00"), new BigDecimal("100000.00"),
                        "Everything in Standard plus Pre-Travel Checklist, Medication Packing List, and Doctor-Ready Clinical Summary Letter. For teams of 100–500.",
                        false, true, "100-500", "PREMIUM"),

                // Enterprise company plans — 500+ signups
                createPlan(CreditPlanCode.ENTERPRISE_PLATINUM, "Enterprise Platinum",
                        new BigDecimal("50.00"), new BigDecimal("50000.00"),
                        "Fully personalised travel health report across 14 clinical decision trees. Designed for large organisations with 500+ members.",
                        false, true, ">500", "STANDARD"),

                createPlan(CreditPlanCode.ENTERPRISE_SIGNATURE, "Enterprise Signature",
                        new BigDecimal("100.00"), new BigDecimal("100000.00"),
                        "Everything in Standard plus Pre-Travel Checklist, Medication Packing List, and Doctor-Ready Clinical Summary Letter. For 500+ member organisations.",
                        false, true, ">500", "PREMIUM")
        );

        repository.saveAll(plans.stream()
                .map(this::mergePlan)
                .toList());
        logger.info("Seeded {} user credit plan entries.", plans.size());
    }

    private CreditPlan mergePlan(CreditPlan seededPlan) {
        CreditPlan plan = repository.findByCode(seededPlan.getCode()).orElse(seededPlan);
        plan.setDisplayName(seededPlan.getDisplayName());
        plan.setBasePriceUsd(seededPlan.getBasePriceUsd());
        plan.setBasePriceNgn(seededPlan.getBasePriceNgn());
        plan.setDescription(seededPlan.getDescription());
        plan.setIsDefault(seededPlan.getIsDefault());
        plan.setIsCompanyPlan(seededPlan.getIsCompanyPlan());
        plan.setIsFamilyPlan(seededPlan.getIsFamilyPlan());
        plan.setSignupRangeLabel(seededPlan.getSignupRangeLabel());
        plan.setServiceLevel(seededPlan.getServiceLevel());
        plan.setVisibility(seededPlan.getVisibility());
        plan.setPlanCount(seededPlan.getPlanCount());
        plan.setIncludedFamilyMembers(seededPlan.getIncludedFamilyMembers());
        plan.setAdditionalMemberPriceUsd(seededPlan.getAdditionalMemberPriceUsd());
        plan.setAdditionalMemberPriceNgn(seededPlan.getAdditionalMemberPriceNgn());
        return plan;
    }

    private CreditPlan createPlan(CreditPlanCode code, String displayName,
                                  BigDecimal basePriceUsd, BigDecimal basePriceNgn,
                                  String description, boolean isDefault,
                                  boolean isCompanyPlan, String signupRangeLabel,
                                  String serviceLevel) {
        CreditPlan plan = new CreditPlan();
        plan.setCode(code);
        plan.setDisplayName(displayName);
        plan.setBasePriceUsd(basePriceUsd);
        plan.setBasePriceNgn(basePriceNgn);
        plan.setDescription(description);
        plan.setIsDefault(isDefault);
        plan.setIsCompanyPlan(isCompanyPlan);
        plan.setIsFamilyPlan(false);
        plan.setSignupRangeLabel(signupRangeLabel);
        plan.setServiceLevel(serviceLevel);
        plan.setVisibility("PUBLIC");
        return plan;
    }

    private CreditPlan createFamilyPlan(CreditPlanCode code, String displayName,
                                        BigDecimal basePriceUsd, BigDecimal basePriceNgn,
                                        String description, Integer planCount) {
        CreditPlan plan = createPlan(code, displayName, basePriceUsd, basePriceNgn,
                description, false, false, null, null);
        plan.setIsFamilyPlan(true);
        plan.setPlanCount(planCount);
        plan.setIncludedFamilyMembers(6);
        plan.setAdditionalMemberPriceUsd(new BigDecimal("30.00"));
        plan.setAdditionalMemberPriceNgn(new BigDecimal("25000.00"));
        return plan;
    }
}
