package com.TravelMedicineAdvisory.Server.domain.country;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "countries")
@SQLDelete(sql = "UPDATE countries SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Country extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    private String region;

    private String continent;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "visa_info", columnDefinition = "TEXT")
    private String visaInfo;

    private String currency;

    private String language;

    private String timezone;

    @Column(name = "health_advisory", columnDefinition = "TEXT")
    private String healthAdvisory;

    @Column(name = "travel_advisory", columnDefinition = "TEXT")
    private String travelAdvisory;

    @Column(name = "emergency_number")
    private String emergencyNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getVisaInfo() {
        return visaInfo;
    }

    public void setVisaInfo(String visaInfo) {
        this.visaInfo = visaInfo;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getHealthAdvisory() {
        return healthAdvisory;
    }

    public void setHealthAdvisory(String healthAdvisory) {
        this.healthAdvisory = healthAdvisory;
    }

    public String getTravelAdvisory() {
        return travelAdvisory;
    }

    public void setTravelAdvisory(String travelAdvisory) {
        this.travelAdvisory = travelAdvisory;
    }

    public String getEmergencyNumber() {
        return emergencyNumber;
    }

    public void setEmergencyNumber(String emergencyNumber) {
        this.emergencyNumber = emergencyNumber;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
