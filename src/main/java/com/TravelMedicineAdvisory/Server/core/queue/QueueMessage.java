package com.TravelMedicineAdvisory.Server.core.queue;

import java.util.Map;

/**
 * Envelope serialized to JSON and stored in Redis.
 * The {@code data} field carries the job-specific payload
 * (e.g. to, subject, variables for email jobs).
 */
public class QueueMessage {

    private String id;
    private JobType type;
    private int attempts;
    private int maxAttempts;
    private Map<String, Object> data;

    public QueueMessage() {}

    public QueueMessage(String id, JobType type, int attempts, int maxAttempts, Map<String, Object> data) {
        this.id = id;
        this.type = type;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.data = data;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public JobType getType() { return type; }
    public void setType(JobType type) { this.type = type; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
