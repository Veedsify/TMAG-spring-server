package com.TravelMedicineAdvisory.Server.domain.abuseflag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbuseFlagRepository extends JpaRepository<AbuseFlag, Long> {
}
