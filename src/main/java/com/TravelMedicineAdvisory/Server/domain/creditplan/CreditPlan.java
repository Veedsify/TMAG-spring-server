package com.TravelMedicineAdvisory.Server.domain.creditplan;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "user_credit_plans")
@SQLDelete(sql = "UPDATE user_credit_plans SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class CreditPlan extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "base_price_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePriceUsd;

    @Column(name = "base_price_ngn", precision = 12, scale = 2)
    private BigDecimal basePriceNgn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_company_plan", nullable = false)
    private Boolean isCompanyPlan = false;

    @Column(name = "is_family_plan", nullable = false)
    private Boolean isFamilyPlan = false;

    @Column(name = "signup_range_label", length = 20)
    private String signupRangeLabel;

    @Column(name = "service_level", length = 20)
    private String serviceLevel;

    @Column(length = 20, nullable = false)
    private String visibility = CreditPlanVisibility.PUBLIC.name();

    @Column(name = "assigned_company_id")
    private Long assignedCompanyId;

    @Column(name = "plan_count")
    private Integer planCount;

    @Column(name = "included_family_members")
    private Integer includedFamilyMembers;

    @Column(name = "additional_member_price_usd", precision = 10, scale = 2)
    private BigDecimal additionalMemberPriceUsd;

    @Column(name = "additional_member_price_ngn", precision = 12, scale = 2)
    private BigDecimal additionalMemberPriceNgn;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCode(CreditPlanCode code) {
        this.code = code != null ? code.name() : null;
    }

    public CreditPlanCode getCodeEnum() {
        if (code == null) {
            return null;
        }
        try {
            return CreditPlanCode.valueOf(code);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BigDecimal getBasePriceUsd() {
        return basePriceUsd;
    }

    public void setBasePriceUsd(BigDecimal basePriceUsd) {
        this.basePriceUsd = basePriceUsd;
    }

    public BigDecimal getBasePriceNgn() {
        return basePriceNgn;
    }

    public void setBasePriceNgn(BigDecimal basePriceNgn) {
        this.basePriceNgn = basePriceNgn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsCompanyPlan() {
        return isCompanyPlan;
    }

    public void setIsCompanyPlan(Boolean isCompanyPlan) {
        this.isCompanyPlan = isCompanyPlan;
    }

    public Boolean getIsFamilyPlan() {
        return isFamilyPlan;
    }

    public void setIsFamilyPlan(Boolean isFamilyPlan) {
        this.isFamilyPlan = isFamilyPlan;
    }

    public String getSignupRangeLabel() {
        return signupRangeLabel;
    }

    public void setSignupRangeLabel(String signupRangeLabel) {
        this.signupRangeLabel = signupRangeLabel;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Long getAssignedCompanyId() {
        return assignedCompanyId;
    }

    public void setAssignedCompanyId(Long assignedCompanyId) {
        this.assignedCompanyId = assignedCompanyId;
    }

    public Integer getPlanCount() {
        return planCount;
    }

    public void setPlanCount(Integer planCount) {
        this.planCount = planCount;
    }

    public Integer getIncludedFamilyMembers() {
        return includedFamilyMembers;
    }

    public void setIncludedFamilyMembers(Integer includedFamilyMembers) {
        this.includedFamilyMembers = includedFamilyMembers;
    }

    public BigDecimal getAdditionalMemberPriceUsd() {
        return additionalMemberPriceUsd;
    }

    public void setAdditionalMemberPriceUsd(BigDecimal additionalMemberPriceUsd) {
        this.additionalMemberPriceUsd = additionalMemberPriceUsd;
    }

    public BigDecimal getAdditionalMemberPriceNgn() {
        return additionalMemberPriceNgn;
    }

    public void setAdditionalMemberPriceNgn(BigDecimal additionalMemberPriceNgn) {
        this.additionalMemberPriceNgn = additionalMemberPriceNgn;
    }
}
