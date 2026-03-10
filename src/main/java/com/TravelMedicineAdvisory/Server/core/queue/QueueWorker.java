package com.TravelMedicineAdvisory.Server.core.queue;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.core.email.EmailTemplates;
import static com.TravelMedicineAdvisory.Server.core.queue.QueueService.KEY_DELAYED;
import static com.TravelMedicineAdvisory.Server.core.queue.QueueService.KEY_FAILED;
import static com.TravelMedicineAdvisory.Server.core.queue.QueueService.KEY_PENDING;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class QueueWorker {

    private static final Logger logger = LoggerFactory.getLogger(QueueWorker.class);
    private static final int BATCH_SIZE = 10;

    private final StringRedisTemplate redis;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;
    private final ObjectMapper objectMapper;

    public QueueWorker(StringRedisTemplate redis, EmailService emailService,
            EmailTemplates emailTemplates, ObjectMapper objectMapper) {
        this.redis = redis;
        this.emailService = emailService;
        this.emailTemplates = emailTemplates;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.queue.worker.interval-ms:5000}")
    public void processJobs() {
        promoteDelayedJobs();

        for (int i = 0; i < BATCH_SIZE; i++) {
            String json = redis.opsForList().rightPop(KEY_PENDING);
            if (json == null) {
                break;
            }
            processMessage(json);
        }
    }

    /**
     * Moves jobs from the delayed sorted set into the pending list when their
     * scheduled time has arrived.
     */
    private void promoteDelayedJobs() {
        long now = System.currentTimeMillis();
        Set<String> ready = redis.opsForZSet().rangeByScore(KEY_DELAYED, 0, now);
        if (ready == null || ready.isEmpty()) {
            return;
        }

        for (String json : ready) {
            redis.opsForZSet().remove(KEY_DELAYED, json);
            redis.opsForList().leftPush(KEY_PENDING, json);
        }
        logger.debug("Promoted {} delayed job(s) to pending", ready.size());
    }

    private void processMessage(String json) {
        QueueMessage msg;
        try {
            msg = objectMapper.readValue(json, QueueMessage.class);
        } catch (JsonProcessingException e) {
            logger.error("Malformed queue message, discarding: {}", e.getMessage());
            return;
        }

        msg.setAttempts(msg.getAttempts() + 1);

        try {
            switch (msg.getType()) {
                case EMAIL_VERIFICATION ->
                    handleEmailJob(msg, "verification");
                case EMAIL_PASSWORD_RESET ->
                    handleEmailJob(msg, "password_reset");
                case EMAIL_PASSWORD_CHANGED ->
                    handleEmailJob(msg, "password_changed");
                case EMAIL_GENERIC ->
                    handleEmailJob(msg, "generic");
            }
            logger.info("Queue job [{}] id={} completed", msg.getType(), msg.getId());

        } catch (Exception e) {
            logger.error("Queue job [{}] id={} failed (attempt {}/{}): {}",
                    msg.getType(), msg.getId(), msg.getAttempts(), msg.getMaxAttempts(), e.getMessage());

            if (msg.getAttempts() >= msg.getMaxAttempts()) {
                // Dead-letter: store for inspection / manual replay
                try {
                    redis.opsForList().leftPush(KEY_FAILED, objectMapper.writeValueAsString(msg));
                } catch (JsonProcessingException ex) {
                    logger.error("Failed to push job to dead-letter queue: {}", ex.getMessage());
                }
                logger.error("Queue job [{}] id={} permanently failed after {} attempts",
                        msg.getType(), msg.getId(), msg.getAttempts());
            } else {
                // Exponential backoff: 2, 4, 8 minutes
                long backoffMs = (long) Math.pow(2, msg.getAttempts()) * 60_000L;
                long processAt = System.currentTimeMillis() + backoffMs;
                try {
                    redis.opsForZSet().add(KEY_DELAYED, objectMapper.writeValueAsString(msg), processAt);
                    logger.info("Queue job [{}] id={} scheduled for retry in {}m",
                            msg.getType(), msg.getId(), backoffMs / 60_000L);
                } catch (JsonProcessingException ex) {
                    logger.error("Failed to schedule retry for job id={}: {}", msg.getId(), ex.getMessage());
                }
            }
        }
    }

    private void handleEmailJob(QueueMessage msg, String templateType) throws Exception {
        Map<String, Object> data = msg.getData();
        String to = (String) data.get("to");
        String subject = (String) data.get("subject");

        @SuppressWarnings("unchecked")
        Map<String, String> vars = data.containsKey("variables")
                ? (Map<String, String>) data.get("variables")
                : Map.of();

        String firstName = vars.getOrDefault("firstName", "there");
        String link = vars.getOrDefault("link", "#");

        String html = switch (templateType) {
            case "verification" ->
                emailTemplates.verificationEmail(firstName, link);
            case "password_reset" ->
                emailTemplates.passwordResetEmail(firstName, link);
            case "password_changed" ->
                emailTemplates.passwordChangedEmail(firstName);
            default ->
                emailTemplates.genericEmail(subject, vars.getOrDefault("content", ""));
        };

        emailService.sendHtmlEmail(to, subject, html);
    }
}
