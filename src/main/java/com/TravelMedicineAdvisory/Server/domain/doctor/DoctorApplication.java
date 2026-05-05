package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
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
@Table(name = "doctor_applications", indexes = {
        @Index(name = "idx_doctor_applications_status_created", columnList = "status, created_at"),
        @Index(name = "idx_doctor_applications_email", columnList = "email")
})
@SQLDelete(sql = "UPDATE doctor_applications SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class DoctorApplication extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(name = "medical_license_number", nullable = false)
    private String medicalLicenseNumber;

    @Column
    private String specialty;

    @Column
    private String country;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "medical_license_url")
    private String medicalLicenseUrl;

    @Column(name = "identity_document_url")
    private String identityDocumentUrl;

    @Column(name = "cv_or_profile_url")
    private String cvOrProfileUrl;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "stamp_url")
    private String stampUrl;

    @Column(name = "confidentiality_agreement_accepted")
    private boolean confidentialityAgreementAccepted;

    @Column(name = "conduct_agreement_accepted")
    private boolean conductAgreementAccepted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoctorApplicationStatus status = DoctorApplicationStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_user_id")
    private User createdUser;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getMedicalLicenseNumber() { return medicalLicenseNumber; }
    public void setMedicalLicenseNumber(String medicalLicenseNumber) { this.medicalLicenseNumber = medicalLicenseNumber; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getMedicalLicenseUrl() { return medicalLicenseUrl; }
    public void setMedicalLicenseUrl(String medicalLicenseUrl) { this.medicalLicenseUrl = medicalLicenseUrl; }
    public String getIdentityDocumentUrl() { return identityDocumentUrl; }
    public void setIdentityDocumentUrl(String identityDocumentUrl) { this.identityDocumentUrl = identityDocumentUrl; }
    public String getCvOrProfileUrl() { return cvOrProfileUrl; }
    public void setCvOrProfileUrl(String cvOrProfileUrl) { this.cvOrProfileUrl = cvOrProfileUrl; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public String getSignatureUrl() { return signatureUrl; }
    public void setSignatureUrl(String signatureUrl) { this.signatureUrl = signatureUrl; }
    public String getStampUrl() { return stampUrl; }
    public void setStampUrl(String stampUrl) { this.stampUrl = stampUrl; }
    public boolean isConfidentialityAgreementAccepted() { return confidentialityAgreementAccepted; }
    public void setConfidentialityAgreementAccepted(boolean confidentialityAgreementAccepted) { this.confidentialityAgreementAccepted = confidentialityAgreementAccepted; }
    public boolean isConductAgreementAccepted() { return conductAgreementAccepted; }
    public void setConductAgreementAccepted(boolean conductAgreementAccepted) { this.conductAgreementAccepted = conductAgreementAccepted; }
    public DoctorApplicationStatus getStatus() { return status; }
    public void setStatus(DoctorApplicationStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public User getCreatedUser() { return createdUser; }
    public void setCreatedUser(User createdUser) { this.createdUser = createdUser; }
}
