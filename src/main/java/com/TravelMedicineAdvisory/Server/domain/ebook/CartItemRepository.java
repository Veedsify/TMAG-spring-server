package com.TravelMedicineAdvisory.Server.domain.ebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByCreatedAtAsc(Long userId);
    Optional<CartItem> findByUserIdAndEbookVersionId(Long userId, Long ebookVersionId);
    void deleteByUserId(Long userId);
    boolean existsByUserIdAndEbookVersionId(Long userId, Long ebookVersionId);
}
