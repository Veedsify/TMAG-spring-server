package com.TravelMedicineAdvisory.Server.domain.draftplan;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "draft_plans")
@SQLDelete(sql = "UPDATE draft_plans SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class DraftPlan extends BaseEntity {

    private String title;
    private String country;

    @Column(columnDefinition = "TEXT")
    private String answersJson;

    private Integer categoryIndex;
    private Boolean showVerify;
    private Boolean showIntro;
    private Boolean riskConsentGiven;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAnswersJson() {
        return answersJson;
    }

    public void setAnswersJson(String answersJson) {
        this.answersJson = answersJson;
    }

    public Integer getCategoryIndex() {
        return categoryIndex;
    }

    public void setCategoryIndex(Integer categoryIndex) {
        this.categoryIndex = categoryIndex;
    }

    public Boolean getShowVerify() {
        return showVerify;
    }

    public void setShowVerify(Boolean showVerify) {
        this.showVerify = showVerify;
    }

    public Boolean getShowIntro() {
        return showIntro;
    }

    public void setShowIntro(Boolean showIntro) {
        this.showIntro = showIntro;
    }

    public Boolean getRiskConsentGiven() {
        return riskConsentGiven;
    }

    public void setRiskConsentGiven(Boolean riskConsentGiven) {
        this.riskConsentGiven = riskConsentGiven;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
