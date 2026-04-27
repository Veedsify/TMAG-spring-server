package com.TravelMedicineAdvisory.Server.domain.travelplan;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanTier;
import com.TravelMedicineAdvisory.Server.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "travel_plans", indexes = {
        @Index(name = "idx_travel_plans_active_created", columnList = "deleted_at, created_at"),
        @Index(name = "idx_travel_plans_user_active_created", columnList = "user_id, deleted_at, created_at"),
        @Index(name = "idx_travel_plans_company_active_created", columnList = "company_id, deleted_at, created_at"),
        @Index(name = "idx_travel_plans_employee_created", columnList = "employee_id, created_at"),
        @Index(name = "idx_travel_plans_status_active_created", columnList = "status, deleted_at, created_at"),
        @Index(name = "idx_travel_plans_doctor_status_updated", columnList = "doctor_validation_status, deleted_at, updated_at"),
        @Index(name = "idx_travel_plans_validator_status_date", columnList = "validated_by_id, doctor_validation_status, deleted_at, validated_at")
})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_tier")
    private PlanTier planTier = PlanTier.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "doctor_validation_status")
    private DoctorValidationStatus doctorValidationStatus = DoctorValidationStatus.NOT_REQUIRED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    private User validatedBy;

    @Column(name = "validated_at")
    private java.time.LocalDateTime validatedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

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

    public PlanTier getPlanTier() {
        return planTier;
    }

    public void setPlanTier(PlanTier planTier) {
        this.planTier = planTier;
    }

    public DoctorValidationStatus getDoctorValidationStatus() {
        return doctorValidationStatus;
    }

    public void setDoctorValidationStatus(DoctorValidationStatus doctorValidationStatus) {
        this.doctorValidationStatus = doctorValidationStatus;
    }

    public User getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(User validatedBy) {
        this.validatedBy = validatedBy;
    }

    public java.time.LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(java.time.LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}