package com.TravelMedicineAdvisory.Server.domain.companyplan;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanService {

    private final PlanRepository repository;

    public PlanService(PlanRepository repository) {
        this.repository = repository;
    }

    public List<PlanResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public PlanResponse findById(Long id) {
        PlanEntity entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Plan not found"));
        return toResponse(entity);
    }

    public PlanResponse create(PlanRequest request) {
        validateRequired(request);
        if (repository.findByCode(request.code()).isPresent()) {
            throw new IllegalArgumentException("Plan code already exists");
        }
        PlanEntity entity = new PlanEntity();
        mapRequestToEntity(request, entity);
        return toResponse(repository.save(entity));
    }

    public PlanResponse update(Long id, PlanRequest request) {
        PlanEntity entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Plan not found"));
        if (request.code() != null && request.code() != entity.getCode()) {
            repository.findByCode(request.code()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Plan code already exists");
                }
            });
        }
        mapRequestToEntity(request, entity);
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Plan not found");
        }
        repository.deleteById(id);
    }

    private void validateRequired(PlanRequest request) {
        if (request.code() == null) {
            throw new IllegalArgumentException("Plan code is required");
        }
        if (request.displayName() == null || request.displayName().isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }
        if (request.signupCredits() == null || request.signupCredits() < 0) {
            throw new IllegalArgumentException("Signup credits must be 0 or greater");
        }
        if (request.maxEmployees() == null || request.maxEmployees() < 1) {
            throw new IllegalArgumentException("Max employees must be at least 1");
        }
    }

    private void mapRequestToEntity(PlanRequest request, PlanEntity entity) {
        if (request.code() != null) {
            entity.setCode(request.code());
        }
        if (request.displayName() != null) {
            entity.setDisplayName(request.displayName());
        }
        if (request.signupCredits() != null) {
            if (request.signupCredits() < 0) {
                throw new IllegalArgumentException("Signup credits must be 0 or greater");
            }
            entity.setSignupCredits(request.signupCredits());
        }
        if (request.maxEmployees() != null) {
            if (request.maxEmployees() < 1) {
                throw new IllegalArgumentException("Max employees must be at least 1");
            }
            entity.setMaxEmployees(request.maxEmployees());
        }
        if (request.customSupportEnabled() != null) {
            entity.setCustomSupportEnabled(request.customSupportEnabled());
        }
        if (request.apiAccessEnabled() != null) {
            entity.setApiAccessEnabled(request.apiAccessEnabled());
        }
        if (request.multipleAdminAccountsEnabled() != null) {
            entity.setMultipleAdminAccountsEnabled(request.multipleAdminAccountsEnabled());
        }
        if (request.highEmployeeLimitEnabled() != null) {
            entity.setHighEmployeeLimitEnabled(request.highEmployeeLimitEnabled());
        }
        if (request.priceUsd() != null) {
            entity.setPriceUsd(request.priceUsd());
        }
        if (request.priceNgn() != null) {
            entity.setPriceNgn(request.priceNgn());
        }
        if (request.priceEur() != null) {
            entity.setPriceEur(request.priceEur());
        }
        if (request.priceGbp() != null) {
            entity.setPriceGbp(request.priceGbp());
        }
    }

    public PlanResponse toResponse(PlanEntity entity) {
        return new PlanResponse(
                entity.getId(),
                entity.getCode() != null ? entity.getCode().name() : null,
                entity.getDisplayName(),
                entity.getSignupCredits(),
                entity.getMaxEmployees(),
                entity.getCustomSupportEnabled(),
                entity.getApiAccessEnabled(),
                entity.getMultipleAdminAccountsEnabled(),
                entity.getHighEmployeeLimitEnabled(),
                entity.getPriceUsd(),
                entity.getPriceNgn(),
                entity.getPriceEur(),
                entity.getPriceGbp(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
