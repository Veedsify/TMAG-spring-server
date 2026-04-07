package com.TravelMedicineAdvisory.Server.domain.ebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EbookOrderRepository extends JpaRepository<EbookOrder, Long> {
    Optional<EbookOrder> findByTxRef(String txRef);
    List<EbookOrder> findAllByTxRef(String txRef);
    List<EbookOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<EbookOrder> findByEbookIdOrderByCreatedAtDesc(Long ebookId);
    List<EbookOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(o) FROM EbookOrder o WHERE o.ebook.id = :ebookId AND o.status = 'completed'")
    long countCompletedByEbookId(@Param("ebookId") Long ebookId);

    @Query("SELECT COALESCE(SUM(o.amountPaid), 0) FROM EbookOrder o WHERE o.ebook.id = :ebookId AND o.status = 'completed'")
    BigDecimal sumRevenueByEbookId(@Param("ebookId") Long ebookId);

    @Query("SELECT COUNT(o) FROM EbookOrder o WHERE o.status = 'completed'")
    long countAllCompleted();

    @Query("SELECT COALESCE(SUM(o.amountPaid), 0) FROM EbookOrder o WHERE o.status = 'completed'")
    BigDecimal sumTotalRevenue();

    boolean existsByUserIdAndEbookIdAndStatus(Long userId, Long ebookId, String status);
    boolean existsByGuestEmailAndEbookIdAndStatus(String guestEmail, Long ebookId, String status);
}
