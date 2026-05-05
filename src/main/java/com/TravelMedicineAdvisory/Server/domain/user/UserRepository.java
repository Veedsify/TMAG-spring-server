package com.TravelMedicineAdvisory.Server.domain.user;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByInvitationToken(String invitationToken);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    List<User> findByType(String type);

    Page<User> findByType(String type, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.type = :type AND u.deletedAt IS NULL")
    List<User> findActiveByType(@Param("type") String type);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    Page<User> findAllActive(Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.type = :type AND u.deletedAt IS NULL")
    long countByType(@Param("type") String type);

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    long countAllActive();

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NOT NULL")
    long countSuspended();

    @Query("SELECT COALESCE(SUM(u.credits), 0) FROM User u WHERE u.type = :type AND u.deletedAt IS NULL")
    long sumCreditsByType(@Param("type") String type);

    @Query("SELECT COALESCE(AVG(u.credits), 0) FROM User u WHERE u.deletedAt IS NULL")
    double averageCreditsActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.lastLogin >= :start AND u.lastLogin < :end")
    long countActiveLastLoginBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.createdAt >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND (LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName AND u.deletedAt IS NULL")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.role.id = :role AND u.deletedAt IS NULL")
    List<User> findByRole(@Param("role") Long role);
}
