package com.TravelMedicineAdvisory.Server.domain.credit;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {
    Page<Credit> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<Credit> findAllByUserId(Long userId, Pageable pageable);

    List<Credit> findByCompanyId(Long companyId, Pageable pageable);

    List<Credit> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Credit> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    @Query("SELECT c FROM Credit c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Credit> findLedgerByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Credit c WHERE c.company.id = :companyId ORDER BY c.createdAt DESC")
    List<Credit> findLedgerByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT c FROM Credit c WHERE c.user.id = :userId OR c.company.id = :companyId ORDER BY c.createdAt DESC")
    List<Credit> findLedgerByUserIdOrCompanyId(@Param("userId") Long userId, @Param("companyId") Long companyId);

    @Query("SELECT c FROM Credit c ORDER BY c.createdAt DESC")
    List<Credit> findAllLedger();

    @Query("SELECT c FROM Credit c ORDER BY c.createdAt DESC")
    Page<Credit> findAllLedger(Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.user.id = :userId")
    Integer sumCreditsByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.company.id = :companyId")
    Integer sumCreditsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.type = 'consume' AND c.user.id = :userId")
    Integer sumConsumedByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Credit c WHERE c.type = 'consume' AND c.company.id = :companyId")
    Integer sumConsumedByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(ABS(c.amount)), 0) FROM Credit c WHERE c.type = 'consume' AND c.user.type = :userType")
    long sumConsumedByUserType(@Param("userType") String userType);

    boolean existsByTypeAndReference(String type, String reference);
}
