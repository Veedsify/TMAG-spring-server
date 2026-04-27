package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Stores questionnaire progress in Redis with a 7-day TTL.
 */
@Service
public class QuestionnaireProgressService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireProgressService.class);
    private static final String KEY_PREFIX = "onboarding:progress:";
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public QuestionnaireProgressService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String email, String progressJson) {
        String key = KEY_PREFIX + email;
        try {
            redisTemplate.opsForValue().set(key, progressJson, TTL);
        } catch (Exception e) {
            logger.warn("Redis unavailable for progress save: {}", e.getMessage());
        }
    }

    public String get(String email) {
        String key = KEY_PREFIX + email;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.warn("Redis unavailable for progress get: {}", e.getMessage());
            return null;
        }
    }

    public void delete(String email) {
        String key = KEY_PREFIX + email;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("Redis unavailable for progress delete: {}", e.getMessage());
        }
    }
}
