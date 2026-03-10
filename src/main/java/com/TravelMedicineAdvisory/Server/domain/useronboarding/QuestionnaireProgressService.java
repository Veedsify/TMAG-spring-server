package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores questionnaire progress in Redis with a 7-day TTL.
 * Falls back to in-memory storage if Redis is unavailable.
 */
@Service
public class QuestionnaireProgressService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireProgressService.class);
    private static final String KEY_PREFIX = "onboarding:progress:";
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;
    // Fallback when Redis is down
    private final ConcurrentHashMap<String, String> memoryFallback = new ConcurrentHashMap<>();

    public QuestionnaireProgressService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(Long userId, String progressJson) {
        String key = KEY_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, progressJson, TTL);
        } catch (Exception e) {
            logger.warn("Redis unavailable for progress save, using memory fallback: {}", e.getMessage());
            memoryFallback.put(key, progressJson);
        }
    }

    public String get(Long userId) {
        String key = KEY_PREFIX + userId;
        try {
            String val = redisTemplate.opsForValue().get(key);
            if (val != null) return val;
        } catch (Exception e) {
            logger.warn("Redis unavailable for progress get, checking memory fallback: {}", e.getMessage());
        }
        return memoryFallback.get(key);
    }

    public void delete(Long userId) {
        String key = KEY_PREFIX + userId;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            logger.warn("Redis unavailable for progress delete: {}", e.getMessage());
        }
        memoryFallback.remove(key);
    }
}
