package com.TravelMedicineAdvisory.Server.domain.ebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EbookVersionRepository extends JpaRepository<EbookVersion, Long> {
    List<EbookVersion> findByEbookIdAndIsActiveTrueOrderBySortOrderAsc(Long ebookId);
    List<EbookVersion> findByEbookIdOrderBySortOrderAsc(Long ebookId);
}
