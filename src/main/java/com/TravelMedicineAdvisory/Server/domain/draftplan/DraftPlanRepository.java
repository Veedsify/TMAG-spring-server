package com.TravelMedicineAdvisory.Server.domain.draftplan;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftPlanRepository extends JpaRepository<DraftPlan, Long> {

    List<DraftPlan> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    @Query("SELECT d FROM DraftPlan d WHERE d.user.id = :userId AND d.deletedAt IS NULL ORDER BY d.updatedAt DESC")
    List<DraftPlan> findActiveByUserId(@Param("userId") Long userId);

    void deleteAllByUserId(Long userId);
}
