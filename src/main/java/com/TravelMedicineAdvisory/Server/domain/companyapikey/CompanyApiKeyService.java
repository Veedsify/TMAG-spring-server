package com.TravelMedicineAdvisory.Server.domain.companyapikey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanCode;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class CompanyApiKeyService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CompanyApiKeyRepository repository;
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final QueueService queueService;

    public CompanyApiKeyService(CompanyApiKeyRepository repository, CompanyRepository companyRepository,
            CompanyUserRepository companyUserRepository, UserRepository userRepository, QueueService queueService) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.userRepository = userRepository;
        this.queueService = queueService;
    }

    public record CreateResult(String fullKey, CompanyApiKeyResponse response) {}

    public CreateResult create(CompanyApiKeyRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        ensureApiAccess(company);

        String rawKey = generateRawKey();
        String keyHash = hashKey(rawKey);
        String keyPrefix = rawKey.substring(0, Math.min(16, rawKey.length()));

        CompanyApiKey entity = new CompanyApiKey();
        entity.setName(request.name());
        entity.setKeyHash(keyHash);
        entity.setKeyPrefix(keyPrefix);
        entity.setCompany(company);
        entity.setStatus(CompanyApiKey.ApiKeyStatus.ACTIVE);
        entity.setExpiresAt(request.expiresAt());

        CompanyApiKey saved = repository.save(entity);

        sendApiKeyEmail(saved, true, saved.getCompany().getName());

        return new CreateResult(rawKey, toResponse(saved));
    }

    private void sendApiKeyEmail(CompanyApiKey apiKey, boolean created, String companyName) {
        CompanyUser firstAdmin = companyUserRepository.findAdminsByCompanyId(apiKey.getCompany().getId()).stream()
                .filter(cu -> cu.getUser() != null && cu.getUser().getEmail() != null)
                .findFirst().orElse(null);

        if (firstAdmin == null || firstAdmin.getUser() == null) {
            return;
        }

        User adminUser = firstAdmin.getUser();
        String firstName = adminUser.getFirstName() != null ? adminUser.getFirstName() : "there";
        if (created) {
            queueService.dispatch(JobType.EMAIL_API_KEY_CREATED, Map.of(
                    "to", adminUser.getEmail(),
                    "subject", "New API key created for " + companyName,
                    "variables", Map.of(
                            "firstName", firstName,
                            "keyName", apiKey.getName(),
                            "companyName", companyName)));
        } else {
            queueService.dispatch(JobType.EMAIL_API_KEY_REVOKED, Map.of(
                    "to", adminUser.getEmail(),
                    "subject", "API key revoked for " + companyName,
                    "variables", Map.of(
                            "firstName", firstName,
                            "keyName", apiKey.getName(),
                            "companyName", companyName)));
        }
    }

    public List<CompanyApiKeyResponse> listByCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        ensureApiAccess(company);
        return repository.findByCompanyIdAndStatus(companyId, CompanyApiKey.ApiKeyStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void revoke(Long id, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        ensureApiAccess(company);

        CompanyApiKey entity = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NoSuchElementException("API key not found"));

        entity.setStatus(CompanyApiKey.ApiKeyStatus.REVOKED);
        repository.save(entity);

        String companyName = entity.getCompany() != null ? entity.getCompany().getName() : "your company";
        sendApiKeyEmail(entity, false, companyName);
    }

    private void ensureApiAccess(Company company) {
        String resolvedPlanCode = company.getCreditPlan() != null && company.getCreditPlan().getCode() != null
                ? company.getCreditPlan().getCode().name()
                : company.getPlan();
        if (!CreditPlanCode.PREMIUM.name().equalsIgnoreCase(resolvedPlanCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "API keys are available for Premium plan companies only");
        }
    }

    private String generateRawKey() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "sk_live_tmag_" + encoded.substring(0, 8) + "_" +
               encoded.substring(8, 12) + "_" +
               encoded.substring(12, 16) + "_" +
               encoded.substring(16, 28);
    }

    private String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private CompanyApiKeyResponse toResponse(CompanyApiKey entity) {
        return new CompanyApiKeyResponse(
                entity.getId(),
                entity.getName(),
                entity.getKeyPrefix(),
                entity.getStatus().name(),
                entity.getLastUsedAt() != null ? entity.getLastUsedAt().toString() : null,
                entity.getExpiresAt() != null ? entity.getExpiresAt().toString() : null,
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null
        );
    }
}
