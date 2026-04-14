package com.TravelMedicineAdvisory.Server.domain.creditplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditPlanRepository extends JpaRepository<CreditPlan, Long> {

    Optional<CreditPlan> findByCode(CreditPlanCode code);

    Optional<CreditPlan> findByIsDefaultTrue();
}
