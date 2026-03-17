package com.TravelMedicineAdvisory.Server.domain.admin.abuse;

import java.time.LocalDateTime;

public class AdminAbuseFlagResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String type;
    private String description;
    private String severity;
    private Boolean resolved;
    private LocalDateTime timestamp;

    public AdminAbuseFlagResponse() {}

    public AdminAbuseFlagResponse(Long id, Long userId, String userName, String type,
                                   String description, String severity, Boolean resolved,
                                   LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.description = description;
        this.severity = severity;
        this.resolved = resolved;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
