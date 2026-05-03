package com.TravelMedicineAdvisory.Server.domain.travelplan;

import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("""
            SELECT new com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanListItemResponse(
                tp.id, tp.destination, tp.country, tp.duration, tp.purpose, tp.riskScore, tp.status,
                c.id, e.id, u.id, tp.createdAt, tp.updatedAt, tp.planTier, tp.doctorValidationStatus,
                CONCAT(COALESCE(v.firstName, ''), ' ', COALESCE(v.lastName, '')),
                tp.validatedAt, tp.rejectionReason, gp.signedPdfUrl, gp.summaryPdfUrl)
            FROM TravelPlan tp
            LEFT JOIN tp.company c
            LEFT JOIN tp.employee e
            LEFT JOIN tp.user u
            LEFT JOIN tp.validatedBy v
            LEFT JOIN GeneratedPlan gp ON gp.travelPlan.id = tp.id AND gp.deletedAt IS NULL
            WHERE u.id = :userId AND tp.deletedAt IS NULL
            ORDER BY tp.createdAt DESC
            """)
    Page<TravelPlanListItemResponse> findListItemsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT new com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanListItemResponse(
                tp.id, tp.destination, tp.country, tp.duration, tp.purpose, tp.riskScore, tp.status,
                c.id, e.id, u.id, tp.createdAt, tp.updatedAt, tp.planTier, tp.doctorValidationStatus,
                CONCAT(COALESCE(v.firstName, ''), ' ', COALESCE(v.lastName, '')),
                tp.validatedAt, tp.rejectionReason, gp.signedPdfUrl, gp.summaryPdfUrl)
            FROM TravelPlan tp
            LEFT JOIN tp.company c
            LEFT JOIN tp.employee e
            LEFT JOIN tp.user u
            LEFT JOIN tp.validatedBy v
            LEFT JOIN GeneratedPlan gp ON gp.travelPlan.id = tp.id AND gp.deletedAt IS NULL
            WHERE c.id = :companyId AND tp.deletedAt IS NULL
            ORDER BY tp.createdAt DESC
            """)
    Page<TravelPlanListItemResponse> findListItemsByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.user.id = :userId AND tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.company.id = :companyId AND tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllActiveByCompanyId(@Param("companyId") Long companyId);

    @Query("""
            SELECT new com.TravelMedicineAdvisory.Server.domain.report.PlanHistoryProjection(
                COALESCE(tp.id, gp.id), COALESCE(tp.destination, gp.destination), tp.country,
                COALESCE(tp.purpose, gp.purpose), tp.tripType, tp.tripDetailsJson,
                COALESCE(tp.duration, gp.duration), COALESCE(tp.riskScore, gp.riskScore),
                COALESCE(tp.status, gp.status), COALESCE(e.name, u.name, gu.name, gu.email),
                tp.medicalConsiderations, tp.vaccinations, tp.healthAlerts, tp.safetyAdvisories,
                tp.medications, tp.waterFood, tp.emergencyContacts, gp.status, gp.planJson,
                gp.signedPdfUrl, gp.summaryPdfUrl, COALESCE(tp.createdAt, gp.createdAt),
                COALESCE(tp.updatedAt, gp.updatedAt))
            FROM GeneratedPlan gp
            LEFT JOIN gp.travelPlan tp
            LEFT JOIN tp.employee e
            LEFT JOIN tp.user u
            LEFT JOIN gp.user gu
            WHERE gp.deletedAt IS NULL
            ORDER BY COALESCE(tp.createdAt, gp.createdAt) DESC
            """)
    List<com.TravelMedicineAdvisory.Server.domain.report.PlanHistoryProjection> findPlanHistoryRows();

    @Query("""
            SELECT new com.TravelMedicineAdvisory.Server.domain.report.PlanHistoryProjection(
                COALESCE(tp.id, gp.id), COALESCE(tp.destination, gp.destination), tp.country,
                COALESCE(tp.purpose, gp.purpose), tp.tripType, tp.tripDetailsJson,
                COALESCE(tp.duration, gp.duration), COALESCE(tp.riskScore, gp.riskScore),
                COALESCE(tp.status, gp.status), COALESCE(e.name, u.name, gu.name, gu.email),
                tp.medicalConsiderations, tp.vaccinations, tp.healthAlerts, tp.safetyAdvisories,
                tp.medications, tp.waterFood, tp.emergencyContacts, gp.status, gp.planJson,
                gp.signedPdfUrl, gp.summaryPdfUrl, COALESCE(tp.createdAt, gp.createdAt),
                COALESCE(tp.updatedAt, gp.updatedAt))
            FROM GeneratedPlan gp
            LEFT JOIN gp.travelPlan tp
            LEFT JOIN tp.employee e
            LEFT JOIN tp.user u
            LEFT JOIN CompanyUser cu ON cu.user.id = u.id AND cu.deletedAt IS NULL
            LEFT JOIN gp.user gu
            LEFT JOIN CompanyUser gcu ON gcu.user.id = gu.id AND gcu.deletedAt IS NULL
            WHERE (gp.company.id = :companyId
                OR tp.company.id = :companyId
                OR e.company.id = :companyId
                OR cu.company.id = :companyId
                OR gcu.company.id = :companyId)
              AND gp.deletedAt IS NULL
              AND (tp.id IS NULL OR tp.deletedAt IS NULL)
            ORDER BY COALESCE(tp.createdAt, gp.createdAt) DESC
            """)
    List<com.TravelMedicineAdvisory.Server.domain.report.PlanHistoryProjection> findPlanHistoryRowsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.status = :status AND tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllActiveByStatus(@Param("status") String status);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.deletedAt IS NULL")
    long countAllActive();

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.user.id = :userId AND tp.deletedAt IS NULL")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.company.id = :companyId AND tp.deletedAt IS NULL")
    long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT tp.destination, COUNT(tp) FROM TravelPlan tp WHERE tp.deletedAt IS NULL AND tp.destination IS NOT NULL GROUP BY tp.destination ORDER BY COUNT(tp) DESC")
    List<Object[]> countActiveByDestination();

    @Query("SELECT YEAR(tp.createdAt), MONTH(tp.createdAt), COUNT(tp) FROM TravelPlan tp WHERE tp.deletedAt IS NULL AND tp.createdAt IS NOT NULL GROUP BY YEAR(tp.createdAt), MONTH(tp.createdAt)")
    List<Object[]> countActiveByCreatedMonth();

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.deletedAt IS NULL AND tp.riskScore < :upper")
    long countActiveRiskBelow(@Param("upper") int upper);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.deletedAt IS NULL AND tp.riskScore >= :lower AND tp.riskScore < :upper")
    long countActiveRiskBetween(@Param("lower") int lower, @Param("upper") int upper);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.deletedAt IS NULL AND tp.riskScore >= :lower")
    long countActiveRiskAtLeast(@Param("lower") int lower);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.deletedAt IS NULL AND (LOWER(tp.destination) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TravelPlan> searchPlans(@Param("search") String search, Pageable pageable);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.status = 'COMPLETED' AND tp.doctorValidationStatus = 'PENDING' AND tp.deletedAt IS NULL ORDER BY tp.createdAt DESC")
    List<TravelPlan> findPendingDoctorValidation();

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.status = 'COMPLETED' AND tp.doctorValidationStatus = 'PENDING' AND tp.deletedAt IS NULL")
    long countPendingDoctorValidation();

    @Query("""
            SELECT tp.id AS planId,
                   tp.destination AS destination,
                   tp.country AS country,
                   tp.purpose AS purpose,
                   tp.duration AS duration,
                   tp.riskScore AS riskScore,
                   tp.doctorValidationStatus AS validationStatus,
                   tp.planTier AS planTier,
                   u.firstName AS travellerFirstName,
                   u.lastName AS travellerLastName,
                   u.name AS travellerName,
                   u.email AS travellerEmail,
                   tp.createdAt AS createdAt,
                   gp.status AS generatedPlanStatus
            FROM TravelPlan tp
            LEFT JOIN tp.user u
            LEFT JOIN GeneratedPlan gp ON gp.travelPlan.id = tp.id AND gp.deletedAt IS NULL
            WHERE tp.status = 'COMPLETED'
              AND tp.doctorValidationStatus = 'PENDING'
              AND tp.deletedAt IS NULL
            ORDER BY tp.createdAt DESC
            """)
    Page<DoctorValidationPlanProjection> findPendingDoctorValidationSummaries(Pageable pageable);

    @Query("""
            SELECT tp.id AS planId,
                   tp.destination AS destination,
                   tp.country AS country,
                   tp.purpose AS purpose,
                   tp.duration AS duration,
                   tp.riskScore AS riskScore,
                   tp.doctorValidationStatus AS validationStatus,
                   tp.planTier AS planTier,
                   u.firstName AS travellerFirstName,
                   u.lastName AS travellerLastName,
                   u.name AS travellerName,
                   u.email AS travellerEmail,
                   tp.createdAt AS createdAt,
                   gp.status AS generatedPlanStatus
            FROM TravelPlan tp
            LEFT JOIN tp.user u
            LEFT JOIN GeneratedPlan gp ON gp.travelPlan.id = tp.id AND gp.deletedAt IS NULL
            WHERE tp.status = 'COMPLETED'
              AND tp.doctorValidationStatus = 'PENDING'
              AND tp.deletedAt IS NULL
              AND (
                NOT EXISTS (
                    SELECT a.id FROM TravelPlanDoctorAssignment a
                    WHERE a.travelPlan.id = tp.id AND a.deletedAt IS NULL
                )
                OR EXISTS (
                    SELECT a2.id FROM TravelPlanDoctorAssignment a2
                    WHERE a2.travelPlan.id = tp.id AND a2.doctor.id = :doctorId AND a2.deletedAt IS NULL
                )
              )
            ORDER BY tp.createdAt DESC
            """)
    Page<DoctorValidationPlanProjection> findPendingDoctorValidationSummariesForDoctor(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.validatedBy.id = :doctorId AND tp.doctorValidationStatus = 'APPROVED' AND tp.deletedAt IS NULL ORDER BY tp.validatedAt DESC")
    List<TravelPlan> findApprovedByDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.validatedBy.id = :doctorId AND tp.doctorValidationStatus = 'REJECTED' AND tp.deletedAt IS NULL ORDER BY tp.validatedAt DESC")
    List<TravelPlan> findRejectedByDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.validatedBy.id = :doctorId AND tp.doctorValidationStatus IN ('APPROVED', 'REJECTED') AND tp.deletedAt IS NULL ORDER BY tp.validatedAt DESC")
    List<TravelPlan> findValidatedByDoctor(@Param("doctorId") Long doctorId);

    @Query("""
            SELECT tp.id AS planId,
                   tp.destination AS destination,
                   tp.country AS country,
                   tp.purpose AS purpose,
                   tp.duration AS duration,
                   tp.riskScore AS riskScore,
                   tp.doctorValidationStatus AS validationStatus,
                   tp.planTier AS planTier,
                   u.firstName AS travellerFirstName,
                   u.lastName AS travellerLastName,
                   u.name AS travellerName,
                   u.email AS travellerEmail,
                   tp.createdAt AS createdAt,
                   gp.status AS generatedPlanStatus
            FROM TravelPlan tp
            LEFT JOIN tp.user u
            LEFT JOIN GeneratedPlan gp ON gp.travelPlan.id = tp.id AND gp.deletedAt IS NULL
            WHERE tp.validatedBy.id = :doctorId
              AND tp.doctorValidationStatus IN ('APPROVED', 'REJECTED')
              AND tp.deletedAt IS NULL
            ORDER BY tp.validatedAt DESC
            """)
    Page<DoctorValidationPlanProjection> findValidatedDoctorValidationSummaries(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.validatedBy.id = :doctorId AND tp.doctorValidationStatus = 'APPROVED' AND tp.deletedAt IS NULL AND FUNCTION('DATE', tp.validatedAt) = CURRENT_DATE")
    long countApprovedByDoctorToday(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.validatedBy.id = :doctorId AND tp.doctorValidationStatus = 'APPROVED' AND tp.deletedAt IS NULL AND tp.validatedAt >= :start AND tp.validatedAt < :end")
    long countApprovedByDoctorBetween(@Param("doctorId") Long doctorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.validatedBy.id = :doctorId AND tp.doctorValidationStatus IN ('APPROVED', 'REJECTED') AND tp.deletedAt IS NULL")
    long countValidatedByDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT tp FROM TravelPlan tp WHERE tp.doctorValidationStatus = :status AND tp.deletedAt IS NULL ORDER BY tp.updatedAt DESC")
    Page<TravelPlan> findByDoctorValidationStatus(@Param("status") DoctorValidationStatus status, Pageable pageable);

    @Query("""
            SELECT tp.id AS id,
                   tp.destination AS destination,
                   tp.duration AS duration,
                   tp.purpose AS purpose,
                   tp.riskScore AS riskScore,
                   traveller.firstName AS travellerFirstName,
                   traveller.lastName AS travellerLastName,
                   traveller.name AS travellerName,
                   traveller.email AS travellerEmail,
                   doctor.firstName AS doctorFirstName,
                   doctor.lastName AS doctorLastName,
                   doctor.name AS doctorName,
                   tp.rejectionReason AS doctorFeedback,
                   gp.signedPdfUrl AS pdfPreviewUrl,
                   gp.summaryPdfUrl AS summaryPreviewUrl,
                    tp.validatedAt AS escalatedAt
             FROM TravelPlan tp
             LEFT JOIN tp.user traveller
             LEFT JOIN tp.validatedBy doctor
             LEFT JOIN GeneratedPlan gp ON gp.travelPlan.id = tp.id AND gp.deletedAt IS NULL
             WHERE tp.doctorValidationStatus = :status AND tp.deletedAt IS NULL
             ORDER BY tp.updatedAt DESC
             """)
     Page<EscalatedPlanProjection> findEscalatedPlanSummaries(@Param("status") DoctorValidationStatus status, Pageable pageable);

    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.validatedBy.id = :doctorId AND tp.doctorValidationStatus = 'APPROVED' AND tp.deletedAt IS NULL")
    long countApprovedByDoctor(@Param("doctorId") Long doctorId);
}
