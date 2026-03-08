package com.TravelMedicineAdvisory.Server.domain.planusageledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanUsageLedgerRepository extends JpaRepository<PlanUsageLedger, Long> {
}
