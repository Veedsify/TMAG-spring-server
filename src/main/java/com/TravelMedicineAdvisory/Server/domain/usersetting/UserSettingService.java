package com.TravelMedicineAdvisory.Server.domain.usersetting;

import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplicationStatus;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class UserSettingService {

    public static final int CURRENT_CONSENT_VERSION = 1;

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

    public UserSettingService(UserSettingRepository userSettingRepository, UserRepository userRepository) {
        this.userSettingRepository = userSettingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserSetting getByUserId(Long userId) {
        return userSettingRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NoSuchElementException("User settings not found"));
    }

    public UserSetting getOrCreateByUserId(Long userId) {
        return userSettingRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    public UserSetting createDefaultSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        UserSetting setting = new UserSetting();
        setting.setUser(user);
        setting.setDoctorApplicationStatus(DoctorApplicationStatus.NONE);
        setting.setConsentVersion(CURRENT_CONSENT_VERSION);
        return userSettingRepository.save(setting);
    }

    public UserSetting updateDoctorFields(Long userId, String medicalLicenseNumber,
            String signatureUrl, String stampUrl, DoctorApplicationStatus status) {
        UserSetting setting = getOrCreateByUserId(userId);
        if (medicalLicenseNumber != null) {
            setting.setMedicalLicenseNumber(medicalLicenseNumber);
        }
        if (signatureUrl != null) {
            setting.setSignatureUrl(signatureUrl);
        }
        if (stampUrl != null) {
            setting.setStampUrl(stampUrl);
        }
        if (status != null) {
            setting.setDoctorApplicationStatus(status);
        }
        return userSettingRepository.save(setting);
    }

    public UserSetting recordConsent(Long userId, String ipAddress) {
        UserSetting setting = getOrCreateByUserId(userId);
        setting.setConsentAcceptedAt(LocalDateTime.now());
        setting.setConsentAcceptedByVersion(CURRENT_CONSENT_VERSION);
        setting.setConsentIp(ipAddress);
        setting.setConsentVersion(CURRENT_CONSENT_VERSION);
        return userSettingRepository.save(setting);
    }

    @Transactional(readOnly = true)
    public boolean hasValidConsent(Long userId) {
        UserSetting setting = userSettingRepository.findByUserIdAndDeletedAtIsNull(userId).orElse(null);
        if (setting == null || setting.getConsentAcceptedByVersion() == null) {
            return false;
        }
        return setting.getConsentAcceptedByVersion() >= CURRENT_CONSENT_VERSION;
    }

    @Transactional(readOnly = true)
    public int getCurrentConsentVersion() {
        return CURRENT_CONSENT_VERSION;
    }

    @Transactional(readOnly = true)
    public List<UserSetting> findByDoctorApplicationStatus(DoctorApplicationStatus status) {
        return userSettingRepository.findWithUserByDoctorApplicationStatusAndDeletedAtIsNull(status);
    }

    @Transactional(readOnly = true)
    public long countByDoctorApplicationStatus(DoctorApplicationStatus status) {
        return userSettingRepository.countByDoctorApplicationStatusAndDeletedAtIsNull(status);
    }
}
