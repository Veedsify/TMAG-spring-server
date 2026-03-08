package com.TravelMedicineAdvisory.Server.domain.pricingplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long> {
    Optional<PricingPlan> findByName(String name);
}
