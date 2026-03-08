package com.TravelMedicineAdvisory.Server.domain.travelrequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, Long> {
}
