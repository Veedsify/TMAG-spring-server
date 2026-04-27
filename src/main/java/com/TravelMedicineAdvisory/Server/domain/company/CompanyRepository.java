package com.TravelMedicineAdvisory.Server.domain.company;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    public Optional<Company> findByCompanyCode(String companyCode);

    List<Company> findByBillingStatus(BillingStatus billingStatus);

    Page<Company> findByBillingStatus(BillingStatus billingStatus, Pageable pageable);

    List<Company> findByTier(Tier tier);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Company> findAllActive();

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Company> findAllActive(Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND c.billingStatus = :status ORDER BY c.createdAt DESC")
    List<Company> findAllActiveByBillingStatus(@Param("status") BillingStatus status);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.deletedAt IS NULL")
    long countAllActive();

    @Query("SELECT COALESCE(SUM(c.totalCredits), 0) FROM Company c WHERE c.deletedAt IS NULL")
    long sumTotalCreditsActive();

    @Query("SELECT COALESCE(SUM(c.usedCredits), 0) FROM Company c WHERE c.deletedAt IS NULL")
    long sumUsedCreditsActive();

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Company> searchCompanies(@Param("search") String search, Pageable pageable);
}
