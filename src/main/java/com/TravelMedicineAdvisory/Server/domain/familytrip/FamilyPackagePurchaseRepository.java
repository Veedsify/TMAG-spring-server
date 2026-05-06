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
    default Optional<FamilyPackagePurchase> findActiveByUserId(Long userId) {
        return findByUserIdAndStatus(userId, FamilyPackagePurchaseStatus.ACTIVE);
    }
}
