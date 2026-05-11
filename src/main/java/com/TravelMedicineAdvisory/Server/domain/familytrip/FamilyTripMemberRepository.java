package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyTripMemberRepository extends JpaRepository<FamilyTripMember, Long> {
    Optional<FamilyTripMember> findBySessionTokenHashAndDeletedAtIsNull(String hash);
    Optional<FamilyTripMember> findByMemberEmailIgnoreCaseAndLoginCodeAndDeletedAtIsNull(String email, String code);
    List<FamilyTripMember> findByFamilyTripIdAndDeletedAtIsNullOrderBySortOrder(Long tripId);

    @Query("SELECT m FROM FamilyTripMember m WHERE m.loginCode = :code AND m.loginCodeConsumedAt IS NULL AND m.deletedAt IS NULL AND m.familyTrip.user.id = :userId")
    Optional<FamilyTripMember> findByLoginCodeAndNotConsumedAndUserId(@Param("code") String code, @Param("userId") Long userId);
}
