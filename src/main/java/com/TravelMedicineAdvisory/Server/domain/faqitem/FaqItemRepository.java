package com.TravelMedicineAdvisory.Server.domain.faqitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqItemRepository extends JpaRepository<FaqItem, Long> {
}
