package com.TravelMedicineAdvisory.Server.domain.plangenerationcontext;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanGenerationContextRepository extends JpaRepository<PlanGenerationContext, Long> {

    @Query("SELECT c FROM PlanGenerationContext c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<PlanGenerationContext> findAllActive();

    @Query("SELECT c FROM PlanGenerationContext c WHERE c.active = true AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<PlanGenerationContext> findEnabled();
}
