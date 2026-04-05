package com.TravelMedicineAdvisory.Server.domain.plangenerationcontext;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "plan_generation_contexts")
@SQLDelete(sql = "UPDATE plan_generation_contexts SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class PlanGenerationContext extends BaseEntity {

    private String title;
    private String sourceType;
    private String fileName;
    private String contentType;
    private String storagePath;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "synthesized_text", columnDefinition = "TEXT")
    private String synthesizedText;

    private Boolean active = true;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getSynthesizedText() {
        return synthesizedText;
    }

    public void setSynthesizedText(String synthesizedText) {
        this.synthesizedText = synthesizedText;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
