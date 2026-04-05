package com.TravelMedicineAdvisory.Server.domain.abuseflag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AbuseFlagRepository extends JpaRepository<AbuseFlag, Long> {

    @Query("SELECT COUNT(a) FROM AbuseFlag a WHERE a.deletedAt IS NULL AND (a.resolved IS NULL OR a.resolved = false)")
    long countUnresolved();
}
