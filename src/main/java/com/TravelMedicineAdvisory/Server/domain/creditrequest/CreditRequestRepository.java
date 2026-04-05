package com.TravelMedicineAdvisory.Server.domain.creditrequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditRequestRepository extends JpaRepository<CreditRequest, Long> {
    Page<CreditRequest> findAllByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT cr FROM CreditRequest cr WHERE cr.company.id = :companyId AND cr.deletedAt IS NULL")
    List<CreditRequest> findAllActiveByCompanyId(@Param("companyId") Long companyId);
}
