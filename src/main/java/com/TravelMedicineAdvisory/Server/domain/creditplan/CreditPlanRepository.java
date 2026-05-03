package com.TravelMedicineAdvisory.Server.domain.creditplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditPlanRepository extends JpaRepository<CreditPlan, Long> {

    Optional<CreditPlan> findByCode(String code);

    default Optional<CreditPlan> findByCode(CreditPlanCode code) {
        return findByCode(code.name());
    }

    Optional<CreditPlan> findByIsDefaultTrue();

    List<CreditPlan> findByVisibilityAndDeletedAtIsNull(String visibility);

    List<CreditPlan> findByAssignedCompanyIdAndDeletedAtIsNull(Long assignedCompanyId);
}
