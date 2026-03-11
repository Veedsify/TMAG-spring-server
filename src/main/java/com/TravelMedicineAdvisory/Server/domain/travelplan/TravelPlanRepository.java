package com.TravelMedicineAdvisory.Server.domain.travelplan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    Page<TravelPlan> findAllByCompanyId(Long companyId, Pageable pageable);
}
