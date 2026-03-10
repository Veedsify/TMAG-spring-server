package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "onboarding_question_categories")
@SQLDelete(sql = "UPDATE onboarding_question_categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class OnboardingQuestionCategory extends BaseEntity {

    @Column(name = "category_key", unique = true, nullable = false)
    private String categoryKey;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "category_icon")
    private String categoryIcon;

    @Column(name = "category_description", columnDefinition = "TEXT")
    private String categoryDescription;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_optional")
    private Boolean isOptional;

    @Column(name = "questions", columnDefinition = "TEXT")
    private String questions; // JSON array of question objects

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsOptional() {
        return isOptional;
    }

    public void setIsOptional(Boolean isOptional) {
        this.isOptional = isOptional;
    }

    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
    }
}
