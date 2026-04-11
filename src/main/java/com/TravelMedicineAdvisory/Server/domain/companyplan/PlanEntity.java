package com.TravelMedicineAdvisory.Server.domain.companyplan;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_plans")
@SQLDelete(sql = "UPDATE company_plans SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class PlanEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanCode code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "signup_credits", nullable = false)
    private Integer signupCredits;

    @Column(name = "max_employees", nullable = false)
    private Integer maxEmployees;

    @Column(name = "custom_support_enabled", nullable = false)
    private Boolean customSupportEnabled = Boolean.FALSE;

    @Column(name = "api_access_enabled", nullable = false)
    private Boolean apiAccessEnabled = Boolean.FALSE;

    @Column(name = "multiple_admin_accounts_enabled", nullable = false)
    private Boolean multipleAdminAccountsEnabled = Boolean.FALSE;

    @Column(name = "high_employee_limit_enabled", nullable = false)
    private Boolean highEmployeeLimitEnabled = Boolean.FALSE;

    @Column(name = "price_usd", nullable = false)
    private BigDecimal priceUsd;

    @Column(name = "price_ngn", nullable = false)
    private BigDecimal priceNgn;

    @Column(name = "price_eur")
    private BigDecimal priceEur;

    @Column(name = "price_gbp")
    private BigDecimal priceGbp;

    public PlanCode getCode() {
        return code;
    }

    public void setCode(PlanCode code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getSignupCredits() {
        return signupCredits;
    }

    public void setSignupCredits(Integer signupCredits) {
        this.signupCredits = signupCredits;
    }

    public Integer getMaxEmployees() {
        return maxEmployees;
    }

    public void setMaxEmployees(Integer maxEmployees) {
        this.maxEmployees = maxEmployees;
    }

    public Boolean getCustomSupportEnabled() {
        return customSupportEnabled;
    }

    public void setCustomSupportEnabled(Boolean customSupportEnabled) {
        this.customSupportEnabled = customSupportEnabled;
    }

    public Boolean getApiAccessEnabled() {
        return apiAccessEnabled;
    }

    public void setApiAccessEnabled(Boolean apiAccessEnabled) {
        this.apiAccessEnabled = apiAccessEnabled;
    }

    public Boolean getMultipleAdminAccountsEnabled() {
        return multipleAdminAccountsEnabled;
    }

    public void setMultipleAdminAccountsEnabled(Boolean multipleAdminAccountsEnabled) {
        this.multipleAdminAccountsEnabled = multipleAdminAccountsEnabled;
    }

    public Boolean getHighEmployeeLimitEnabled() {
        return highEmployeeLimitEnabled;
    }

    public void setHighEmployeeLimitEnabled(Boolean highEmployeeLimitEnabled) {
        this.highEmployeeLimitEnabled = highEmployeeLimitEnabled;
    }

    public BigDecimal getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(BigDecimal priceUsd) {
        this.priceUsd = priceUsd;
    }

    public BigDecimal getPriceNgn() {
        return priceNgn;
    }

    public void setPriceNgn(BigDecimal priceNgn) {
        this.priceNgn = priceNgn;
    }

    public BigDecimal getPriceEur() {
        return priceEur;
    }

    public void setPriceEur(BigDecimal priceEur) {
        this.priceEur = priceEur;
    }

    public BigDecimal getPriceGbp() {
        return priceGbp;
    }

    public void setPriceGbp(BigDecimal priceGbp) {
        this.priceGbp = priceGbp;
    }
}
