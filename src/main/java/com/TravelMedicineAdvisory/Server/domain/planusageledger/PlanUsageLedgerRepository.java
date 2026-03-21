package com.TravelMedicineAdvisory.Server.domain.planusageledger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanUsageLedgerRepository extends JpaRepository<PlanUsageLedger, Long> {

    List<PlanUsageLedger> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<PlanUsageLedger> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT l FROM PlanUsageLedger l WHERE l.travelPlan.employee.id = :employeeId ORDER BY l.createdAt DESC")
    List<PlanUsageLedger> findByEmployeeIdOrderByCreatedAtDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT l FROM PlanUsageLedger l WHERE l.travelPlan.employee.id = :employeeId ORDER BY l.createdAt DESC")
    Page<PlanUsageLedger> findByEmployeeIdOrderByCreatedAtDesc(@Param("employeeId") Long employeeId, Pageable pageable);
}
