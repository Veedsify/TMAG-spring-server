package com.TravelMedicineAdvisory.Server.domain.travelplan;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "travel_plans")
@SQLDelete(sql = "UPDATE travel_plans SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class TravelPlan extends BaseEntity {

    private String destination;
    private String country;
    private Integer duration;
    private String purpose;
    private String tripType;
    @Column(columnDefinition = "TEXT")
    private String tripDetailsJson;
    private Integer riskScore;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String medicalConsiderations;
    @Column(columnDefinition = "TEXT")
    private String vaccinations;
    @Column(columnDefinition = "TEXT")
    private String healthAlerts;
    @Column(columnDefinition = "TEXT")
    private String safetyAdvisories;
    @Column(columnDefinition = "TEXT")
    private String medications;
    @Column(columnDefinition = "TEXT")
    private String waterFood;
    @Column(columnDefinition = "TEXT")
    private String emergencyContacts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getTripDetailsJson() {
        return tripDetailsJson;
    }

    public void setTripDetailsJson(String tripDetailsJson) {
        this.tripDetailsJson = tripDetailsJson;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMedicalConsiderations() {
        return medicalConsiderations;
    }

    public void setMedicalConsiderations(String medicalConsiderations) {
        this.medicalConsiderations = medicalConsiderations;
    }

    public String getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(String vaccinations) {
        this.vaccinations = vaccinations;
    }

    public String getHealthAlerts() {
        return healthAlerts;
    }

    public void setHealthAlerts(String healthAlerts) {
        this.healthAlerts = healthAlerts;
    }

    public String getSafetyAdvisories() {
        return safetyAdvisories;
    }

    public void setSafetyAdvisories(String safetyAdvisories) {
        this.safetyAdvisories = safetyAdvisories;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getWaterFood() {
        return waterFood;
    }

    public void setWaterFood(String waterFood) {
        this.waterFood = waterFood;
    }

    public String getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(String emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}