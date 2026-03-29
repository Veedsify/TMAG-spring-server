package com.TravelMedicineAdvisory.Server.domain.newsletter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterRepository extends JpaRepository<NewsletterSubscriber, Long> {
    boolean existsByEmail(String email);
    Optional<NewsletterSubscriber> findByEmail(String email);
}
