package com.TravelMedicineAdvisory.Server.domain.airequestlog;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class AiRequestLogService {

    private final AiRequestLogRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public AiRequestLogService(AiRequestLogRepository repository, CompanyRepository companyRepository, UserRepository userRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Page<AiRequestLogResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public AiRequestLogResponse findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("AiRequestLog ID cannot be null");
        }
        AiRequestLog entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("AiRequestLog not found"));
        return toResponse(entity);
    }

    public AiRequestLogResponse create(AiRequestLogRequest request) {
        AiRequestLog entity = new AiRequestLog();
        mapRequestToEntity(request, entity);
        AiRequestLog saved = repository.save(entity);
        return toResponse(saved);
    }

    public AiRequestLogResponse update(Long id, AiRequestLogRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("AiRequestLog ID cannot be null");
        }
        AiRequestLog entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("AiRequestLog not found"));
        mapRequestToEntity(request, entity);
        AiRequestLog saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("AiRequestLog ID cannot be null");
        }
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("AiRequestLog not found");
        }
        repository.deleteById(id);
    }

    private AiRequestLogResponse toResponse(AiRequestLog entity) {
        return new AiRequestLogResponse(
            entity.getId(),
            entity.getDestination(),
            entity.getPromptSummary(),
            entity.getOutputSummary(),
            entity.getTokensUsed(),
            entity.getPlanGenerationTokensUsed(),
            entity.getSummaryGenerationTokensUsed(),
            entity.getProcessingTimeMs(),
            entity.getStatus(),
            entity.getErrorMessage(),
            entity.getRiskLevel(),
            entity.getModelUsed(),
            entity.getCreditConsumed(),
            entity.getCompany() != null ? entity.getCompany().getId() : null,
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(AiRequestLogRequest request, AiRequestLog entity) {
        entity.setDestination(request.destination());
        entity.setPromptSummary(request.promptSummary());
        entity.setOutputSummary(request.outputSummary());
        entity.setTokensUsed(request.tokensUsed());
        entity.setPlanGenerationTokensUsed(request.planGenerationTokensUsed());
        entity.setSummaryGenerationTokensUsed(request.summaryGenerationTokensUsed());
        entity.setProcessingTimeMs(request.processingTimeMs());
        entity.setStatus(request.status());
        entity.setErrorMessage(request.errorMessage());
        entity.setRiskLevel(request.riskLevel());
        entity.setModelUsed(request.modelUsed());
        entity.setCreditConsumed(request.creditConsumed());
        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new NoSuchElementException("Company not found"));
            entity.setCompany(company);
        }
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
