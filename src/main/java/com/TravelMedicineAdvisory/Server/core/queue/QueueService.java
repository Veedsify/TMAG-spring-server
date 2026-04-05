package com.TravelMedicineAdvisory.Server.core.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Dispatches jobs onto Redis queues.
 *
 * Keys:
 *   tmag:queue:pending  — List  (LPUSH to enqueue, RPOP to consume)
 *   tmag:queue:delayed  — ZSet  (score = process-at epoch-ms, for retries)
 *   tmag:queue:failed   — List  (dead-letter; permanently failed jobs)
 */
@Service
public class QueueService {

    static final String KEY_PENDING = "tmag:queue:pending";
    static final String KEY_DELAYED = "tmag:queue:delayed";
    static final String KEY_FAILED  = "tmag:queue:failed";

    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public QueueService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    /** Enqueue a job for immediate processing. */
    public void dispatch(JobType type, Object payload) {
        try {
            Map<String, Object> data = objectMapper.convertValue(payload, new TypeReference<>() {});
            QueueMessage msg = new QueueMessage(UUID.randomUUID().toString(), type, 0, 3, data);
            redis.opsForList().leftPush(KEY_PENDING, objectMapper.writeValueAsString(msg));
            if (type == JobType.GENERATE_TRAVEL_PLAN) {
                logger.info("Queued GENERATE_TRAVEL_PLAN job id={} travelPlanId={} userId={}",
                        msg.getId(),
                        data.get("travelPlanId"),
                        data.get("userId"));
            } else {
                logger.debug("Dispatched queue job [{}] id={}", type, msg.getId());
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize queue job [{}]: {}", type, e.getMessage());
            throw new RuntimeException("Failed to dispatch queue job", e);
        }
    }

    public QueueStats getStats() {
        Long pending = redis.opsForList().size(KEY_PENDING);
        Long delayed = redis.opsForZSet().size(KEY_DELAYED);
        Long failed  = redis.opsForList().size(KEY_FAILED);
        return new QueueStats(
                pending != null ? pending : 0,
                delayed != null ? delayed : 0,
                failed  != null ? failed  : 0
        );
    }

    public record QueueStats(long pending, long delayed, long failed) {}
}
