package com.TravelMedicineAdvisory.Server.domain.companyapikey;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyApiKeyRepository extends JpaRepository<CompanyApiKey, Long> {

    List<CompanyApiKey> findByCompanyIdAndStatus(Long companyId, CompanyApiKey.ApiKeyStatus status);

    List<CompanyApiKey> findByCompanyId(Long companyId);

    Optional<CompanyApiKey> findByIdAndCompanyId(Long id, Long companyId);

    Optional<CompanyApiKey> findByKeyHash(String keyHash);
}
