package com.TravelMedicineAdvisory.Server.domain.usersetting;

import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplicationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {

    Optional<UserSetting> findByUserId(Long userId);

    Optional<UserSetting> findByUserIdAndDeletedAtIsNull(Long userId);

    List<UserSetting> findByDoctorApplicationStatus(DoctorApplicationStatus status);

    List<UserSetting> findByDoctorApplicationStatusAndDeletedAtIsNull(DoctorApplicationStatus status);
}
