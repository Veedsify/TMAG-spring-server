package com.TravelMedicineAdvisory.Server.domain.healthprofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
}
