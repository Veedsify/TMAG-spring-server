package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyTripRepository extends JpaRepository<FamilyTrip, Long> {
    Optional<FamilyTrip> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, FamilyTripStatus status);
    List<FamilyTrip> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
}
