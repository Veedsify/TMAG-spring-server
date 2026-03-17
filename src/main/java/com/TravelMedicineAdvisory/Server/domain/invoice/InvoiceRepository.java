package com.TravelMedicineAdvisory.Server.domain.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByCompanyId(Long companyId);

    List<Invoice> findByUserId(Long userId);

    List<Invoice> findByStatus(String status);

    @Query("SELECT i FROM Invoice i WHERE i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<Invoice> findAllActive();

    @Query("SELECT i FROM Invoice i WHERE i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    Page<Invoice> findAllActive(Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.company.id = :companyId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<Invoice> findAllActiveByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<Invoice> findAllActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<Invoice> findAllActiveByStatus(@Param("status") String status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.deletedAt IS NULL")
    long countAllActive();

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status AND i.deletedAt IS NULL")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'paid' AND i.deletedAt IS NULL")
    Long sumPaidInvoices();

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'pending' AND i.deletedAt IS NULL")
    Long sumPendingInvoices();
}
