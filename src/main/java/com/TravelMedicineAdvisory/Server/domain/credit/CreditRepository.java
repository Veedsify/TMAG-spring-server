package com.TravelMedicineAdvisory.Server.domain.credit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {
    Page<Credit> findAllByCompanyId(Long companyId, Pageable pageable);
}
