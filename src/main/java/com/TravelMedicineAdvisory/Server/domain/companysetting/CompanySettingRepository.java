package com.TravelMedicineAdvisory.Server.domain.companysetting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySettingRepository extends JpaRepository<CompanySetting, Long> {

    List<CompanySetting> findByCompanyId(Long companyId);

    Optional<CompanySetting> findByCompanyIdAndKey(Long companyId, String key);

    Optional<CompanySetting> findByCompanyIdAndKeyAndDeletedAtIsNull(Long companyId, String key);

    void deleteByCompanyId(Long companyId);
}
