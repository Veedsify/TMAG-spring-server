package com.TravelMedicineAdvisory.Server.domain.admin.doctor;

public record AdminDoctorApplicationDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String licenseNumber,
        String specialty,
        String country,
        String applicationStatus,
        String applicationSubmittedAt,
        String identityDocumentUrl,
        String licenseDocumentUrl,
        String cvOrProfileUrl,
        String practicingLicenseUrl,
        String travelMedicineCertificateUrl,
        boolean confidentialityAgreementAccepted,
        boolean conductAgreementAccepted,
        String bio,
        String createdAt) {
}
