package com.TravelMedicineAdvisory.Server.domain.creditpurchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditPurchaseRepository extends JpaRepository<CreditPurchase, Long> {
    Optional<CreditPurchase> findByTxRef(String txRef);
    
    Optional<CreditPurchase> findByFlwRef(String flwRef);
    
    List<CreditPurchase> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<CreditPurchase> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<CreditPurchase> findByStatus(String status);
    
    @Query("SELECT cp FROM CreditPurchase cp WHERE cp.status = 'pending' ORDER BY cp.createdAt ASC")
    List<CreditPurchase> findPendingPurchases();
    
    @Query("SELECT cp FROM CreditPurchase cp WHERE cp.user.id = :userId AND cp.txRef = :txRef")
    Optional<CreditPurchase> findByUserIdAndTxRef(@Param("userId") Long userId, @Param("txRef") String txRef);
    
    @Query("SELECT COUNT(cp) FROM CreditPurchase cp WHERE cp.user.id = :userId AND cp.status = 'completed'")
    long countCompletedByUserId(@Param("userId") Long userId);
}
