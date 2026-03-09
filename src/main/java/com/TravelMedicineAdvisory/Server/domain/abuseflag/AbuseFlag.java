package com.TravelMedicineAdvisory.Server.domain.abuseflag;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "abuse_flags")
@SQLDelete(sql = "UPDATE abuse_flags SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class AbuseFlag extends BaseEntity {

    private String type;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String severity;
    private Boolean resolved;
    @Column(name = "resolved_by")
    private Long resolvedBy;
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
