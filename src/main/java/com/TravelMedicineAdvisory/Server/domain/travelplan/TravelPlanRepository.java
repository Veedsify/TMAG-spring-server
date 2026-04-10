package com.TravelMedicineAdvisory.Server.domain.travelplan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    Page<TravelPlan> findAllByCompanyId(Long companyId, Pageable pageable);
    Page<TravelPlan> findAllByUserId(Long userId, Pageable pageable);

    List<TravelPlan> findByUserId(Long userId, Pageable pageable);

    List<TravelPlan> findByCompanyId(Long companyId);

    List<TravelPlan> findByEmployeeId(Long employeeId);

    List<TravelPlan> findByStatus(String status);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllActive();

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    Page<TravelPlan> findAllActive(Pageable pageable);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.user.id = :userId AND tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.company.id = :companyId AND tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllActiveByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.status = :status AND tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllActiveByStatus(@Param("status") String status);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.deletedAt IS NULL")
    long countAllActive();

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.user.id = :userId AND tp.deletedAt IS NULL")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.company.id = :companyId AND tp.deletedAt IS NULL")
    long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.deletedAt IS NULL AND (LOWER(tp.destination) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TravelPlan> searchPlans(@Param("search") String search, Pageable pageable);
}
