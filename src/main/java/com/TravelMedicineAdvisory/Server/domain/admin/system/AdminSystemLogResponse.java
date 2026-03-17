package com.TravelMedicineAdvisory.Server.domain.admin.system;

import java.time.LocalDateTime;

public class AdminSystemLogResponse {
    private Long id;
    private String level;
    private String message;
    private String source;
    private LocalDateTime timestamp;
    private String details;

    public AdminSystemLogResponse() {}

    public AdminSystemLogResponse(Long id, String level, String message, String source,
                                  LocalDateTime timestamp, String details) {
        this.id = id;
        this.level = level;
        this.message = message;
        this.source = source;
        this.timestamp = timestamp;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
