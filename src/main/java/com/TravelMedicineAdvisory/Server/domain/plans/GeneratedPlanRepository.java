package com.TravelMedicineAdvisory.Server.domain.plans;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedPlanRepository extends JpaRepository<GeneratedPlan, Long> {

    Optional<GeneratedPlan> findByTravelPlanId(Long travelPlanId);

    @Query("SELECT gp FROM GeneratedPlan gp WHERE gp.deletedAt IS NULL ORDER BY gp.createdAt DESC")
    List<GeneratedPlan> findAllActive();

    @Query("SELECT gp FROM GeneratedPlan gp WHERE gp.user.id = :userId AND gp.deletedAt IS NULL ORDER BY gp.createdAt DESC")
    List<GeneratedPlan> findAllActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT gp FROM GeneratedPlan gp WHERE gp.company.id = :companyId AND gp.deletedAt IS NULL ORDER BY gp.createdAt DESC")
    List<GeneratedPlan> findAllActiveByCompanyId(@Param("companyId") Long companyId);
}
