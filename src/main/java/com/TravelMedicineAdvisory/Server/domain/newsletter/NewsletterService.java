package com.TravelMedicineAdvisory.Server.domain.newsletter;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.newsletter.NewsletterDto.NewsletterRequest;
import com.TravelMedicineAdvisory.Server.domain.newsletter.NewsletterDto.NewsletterResponse;

@Service
public class NewsletterService {

    private final NewsletterRepository repository;
    private final QueueService queueService;

    public NewsletterService(NewsletterRepository repository, QueueService queueService) {
        this.repository = repository;
        this.queueService = queueService;
    }

    @Transactional
    public NewsletterResponse subscribe(NewsletterRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (repository.existsByEmail(request.email())) {
            NewsletterSubscriber existing = repository.findByEmail(request.email()).orElseThrow();

            if (Boolean.TRUE.equals(existing.getIsActive())) {
                throw new IllegalStateException("Already subscribed");
            }

            existing.setIsActive(true);
            NewsletterSubscriber saved = repository.save(existing);
            dispatchWelcomeEmail(request.email());
            return toResponse(saved);
        }

        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail(request.email());
        subscriber.setIsActive(true);
        NewsletterSubscriber saved = repository.save(subscriber);
        dispatchWelcomeEmail(request.email());
        return toResponse(saved);
    }

    private void dispatchWelcomeEmail(String email) {
        queueService.dispatch(JobType.EMAIL_NEWSLETTER_WELCOME, Map.of(
                "to", email,
                "subject", "Welcome to TMAG updates",
                "variables", Map.of("firstName", "there")));
    }

    private NewsletterResponse toResponse(NewsletterSubscriber s) {
        return new NewsletterResponse(
                s.getId(),
                s.getEmail(),
                s.getIsActive(),
                s.getCreatedAt());
    }
}
