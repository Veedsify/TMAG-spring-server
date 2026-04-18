package com.TravelMedicineAdvisory.Server.domain.companysetting;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.company.Company;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "company_settings", uniqueConstraints = @UniqueConstraint(columnNames = { "company_id", "setting_key" }))
@SQLDelete(sql = "UPDATE company_settings SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class CompanySetting extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "setting_key", nullable = false)
    private String key;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_type")
    private SettingType type = SettingType.STRING;

    public enum SettingType {
        BOOLEAN, STRING, NUMBER
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SettingType getType() {
        return type;
    }

    public void setType(SettingType type) {
        this.type = type;
    }
}
