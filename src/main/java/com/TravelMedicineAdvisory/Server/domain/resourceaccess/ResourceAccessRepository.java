package com.TravelMedicineAdvisory.Server.domain.resourceaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceAccessRepository extends JpaRepository<ResourceAccess, Long> {
}
