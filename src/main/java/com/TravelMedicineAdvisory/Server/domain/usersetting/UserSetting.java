package com.TravelMedicineAdvisory.Server.domain.usersetting;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplicationStatus;
import com.TravelMedicineAdvisory.Server.domain.user.User;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings", indexes = {
    @Index(name = "idx_user_settings_user", columnList = "user_id"),
    @Index(name = "idx_user_settings_doctor_status", columnList = "doctor_application_status")
})
@SQLDelete(sql = "UPDATE user_settings SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class UserSetting extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "medical_license_number")
    private String medicalLicenseNumber;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "stamp_url")
    private String stampUrl;

    @Column(name = "practicing_license_url", length = 1024)
    private String practicingLicenseUrl;

    @Column(name = "travel_medicine_certificate_url", length = 1024)
    private String travelMedicineCertificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "doctor_application_status")
    private DoctorApplicationStatus doctorApplicationStatus = DoctorApplicationStatus.NONE;

    @Column(name = "consent_version")
    private Integer consentVersion = 0;

    @Column(name = "consent_accepted_at")
    private LocalDateTime consentAcceptedAt;

    @Column(name = "consent_accepted_by_version")
    private Integer consentAcceptedByVersion;

    @Column(name = "consent_ip")
    private String consentIp;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMedicalLicenseNumber() {
        return medicalLicenseNumber;
    }

    public void setMedicalLicenseNumber(String medicalLicenseNumber) {
        this.medicalLicenseNumber = medicalLicenseNumber;
    }

    public String getSignatureUrl() {
        return signatureUrl;
    }

    public void setSignatureUrl(String signatureUrl) {
        this.signatureUrl = signatureUrl;
    }

    public String getStampUrl() {
        return stampUrl;
    }

    public void setStampUrl(String stampUrl) {
        this.stampUrl = stampUrl;
    }

    public String getPracticingLicenseUrl() {
        return practicingLicenseUrl;
    }

    public void setPracticingLicenseUrl(String practicingLicenseUrl) {
        this.practicingLicenseUrl = practicingLicenseUrl;
    }

    public String getTravelMedicineCertificateUrl() {
        return travelMedicineCertificateUrl;
    }

    public void setTravelMedicineCertificateUrl(String travelMedicineCertificateUrl) {
        this.travelMedicineCertificateUrl = travelMedicineCertificateUrl;
    }

    public DoctorApplicationStatus getDoctorApplicationStatus() {
        return doctorApplicationStatus;
    }

    public void setDoctorApplicationStatus(DoctorApplicationStatus doctorApplicationStatus) {
        this.doctorApplicationStatus = doctorApplicationStatus;
    }

    public Integer getConsentVersion() {
        return consentVersion;
    }

    public void setConsentVersion(Integer consentVersion) {
        this.consentVersion = consentVersion;
    }

    public LocalDateTime getConsentAcceptedAt() {
        return consentAcceptedAt;
    }

    public void setConsentAcceptedAt(LocalDateTime consentAcceptedAt) {
        this.consentAcceptedAt = consentAcceptedAt;
    }

    public Integer getConsentAcceptedByVersion() {
        return consentAcceptedByVersion;
    }

    public void setConsentAcceptedByVersion(Integer consentAcceptedByVersion) {
        this.consentAcceptedByVersion = consentAcceptedByVersion;
    }

    public String getConsentIp() {
        return consentIp;
    }

    public void setConsentIp(String consentIp) {
        this.consentIp = consentIp;
    }
}
