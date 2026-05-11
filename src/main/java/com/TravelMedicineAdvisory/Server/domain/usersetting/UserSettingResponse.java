package com.TravelMedicineAdvisory.Server.domain.usersetting;

import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplicationStatus;

import java.time.LocalDateTime;

public record UserSettingResponse(
    String medicalLicenseNumber,
    String signatureUrl,
    String stampUrl,
    String doctorApplicationStatus,
    Integer consentVersion,
    Integer consentAcceptedByVersion,
    LocalDateTime consentAcceptedAt,
    String affiliateReferralCode
) {
    public static UserSettingResponse from(UserSetting s) {
        return new UserSettingResponse(
            s.getMedicalLicenseNumber(),
            s.getSignatureUrl(),
            s.getStampUrl(),
            s.getDoctorApplicationStatus() != null ? s.getDoctorApplicationStatus().name() : DoctorApplicationStatus.NONE.name(),
            s.getConsentVersion() != null ? s.getConsentVersion() : 0,
            s.getConsentAcceptedByVersion(),
            s.getConsentAcceptedAt(),
            s.getAffiliateReferralCode()
        );
    }

    public boolean isConsentValid() {
        return consentAcceptedByVersion != null
            && consentVersion != null
            && consentAcceptedByVersion >= consentVersion;
    }
}
