package com.TravelMedicineAdvisory.Server.domain.creditplan;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;


import java.math.BigDecimal;

@Entity
@Table(name = "user_credit_plans")
@SQLDelete(sql = "UPDATE user_credit_plans SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")

public class CreditPlan extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CreditPlanCode code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "base_price_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePriceUsd;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    public CreditPlanCode getCode() {
        return code;
    }

    public void setCode(CreditPlanCode code) {
        this.code = code;
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
}
