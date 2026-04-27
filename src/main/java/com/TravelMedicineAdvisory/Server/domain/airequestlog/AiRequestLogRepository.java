package com.TravelMedicineAdvisory.Server.domain.airequestlog;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {

    @Query("SELECT COUNT(a) FROM AiRequestLog a WHERE a.deletedAt IS NULL AND a.createdAt >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM AiRequestLog a WHERE a.deletedAt IS NULL AND a.createdAt >= :since AND LOWER(a.status) = LOWER(:status)")
    long countCreatedSinceWithStatus(@Param("since") LocalDateTime since, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(a.tokensUsed), 0) FROM AiRequestLog a WHERE a.deletedAt IS NULL AND a.createdAt >= :since")
    long sumTokensUsedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM AiRequestLog a WHERE a.deletedAt IS NULL AND LOWER(a.status) = LOWER(:status)")
    long countByStatusActive(@Param("status") String status);

    @Query("SELECT HOUR(a.createdAt), COUNT(a) FROM AiRequestLog a WHERE a.deletedAt IS NULL AND a.createdAt IS NOT NULL GROUP BY HOUR(a.createdAt)")
    List<Object[]> countActiveByCreatedHour();

    @Query("SELECT a.modelUsed, COUNT(a) FROM AiRequestLog a WHERE a.deletedAt IS NULL AND a.modelUsed IS NOT NULL AND a.modelUsed <> '' GROUP BY a.modelUsed ORDER BY COUNT(a) DESC")
    List<Object[]> countActiveByModel();

    @Query("SELECT YEAR(a.createdAt), MONTH(a.createdAt), COUNT(a) FROM AiRequestLog a WHERE a.deletedAt IS NULL AND a.createdAt IS NOT NULL GROUP BY YEAR(a.createdAt), MONTH(a.createdAt)")
    List<Object[]> countActiveByCreatedMonth();
}
