package com.TravelMedicineAdvisory.Server.domain.familytrip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyPackagePurchaseRepository extends JpaRepository<FamilyPackagePurchase, Long> {
    Optional<FamilyPackagePurchase> findByTxRef(String txRef);
    List<FamilyPackagePurchase> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<FamilyPackagePurchase> findByUserIdAndStatus(Long userId, FamilyPackagePurchaseStatus status);
    List<FamilyPackagePurchase> findAllByUserIdAndStatus(Long userId, FamilyPackagePurchaseStatus status);
    default Optional<FamilyPackagePurchase> findActiveByUserId(Long userId) {
        return findByUserIdAndStatus(userId, FamilyPackagePurchaseStatus.ACTIVE);
    }

    /**
     * Find any active purchase that still has remaining trips (tripsUsed < tripsAllowed).
     * Supports users who have purchased multiple family plans.
     */
    default Optional<FamilyPackagePurchase> findAvailableByUserId(Long userId) {
        return findAllByUserIdAndStatus(userId, FamilyPackagePurchaseStatus.ACTIVE)
                .stream()
                .filter(p -> p.getTripsUsed() < p.getTripsAllowed())
                .findFirst();
    }

    /**
     * Find all active purchases with remaining trips for a user.
     */
    default List<FamilyPackagePurchase> findAllAvailableByUserId(Long userId) {
        return findAllByUserIdAndStatus(userId, FamilyPackagePurchaseStatus.ACTIVE)
                .stream()
                .filter(p -> p.getTripsUsed() < p.getTripsAllowed())
                .toList();
    }
}
