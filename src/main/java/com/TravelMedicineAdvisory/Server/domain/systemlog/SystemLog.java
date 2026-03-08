package com.TravelMedicineAdvisory.Server.domain.systemlog;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Table(name = "system_logs")
@SQLDelete(sql = "UPDATE system_logs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class SystemLog extends BaseEntity {

    private String level;
    @Column(columnDefinition = "TEXT")
    private String message;
    private String source;
    @Column(columnDefinition = "TEXT")
    private String details;


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
