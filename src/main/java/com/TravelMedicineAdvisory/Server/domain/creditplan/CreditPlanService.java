package com.TravelMedicineAdvisory.Server.domain.creditplan;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;

@Service
@Transactional
public class CreditPlanService {

    private final CreditPlanRepository repository;

    public CreditPlanService(CreditPlanRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = CacheNames.USER_CREDIT_PLANS)
    @Transactional(readOnly = true)
    public List<CreditPlanResponse> findAll() {
        return repository.findByVisibilityAndDeletedAtIsNull(CreditPlanVisibility.PUBLIC.name()).stream()
                .map(CreditPlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CreditPlanResponse> findPublicAndCompanyPlans(Long companyId) {
        List<CreditPlan> plans = new java.util.ArrayList<>(
                repository.findByVisibilityAndDeletedAtIsNull(CreditPlanVisibility.PUBLIC.name()));
        if (companyId != null) {
            plans.addAll(repository.findByAssignedCompanyIdAndDeletedAtIsNull(companyId));
        }
        return plans.stream().map(CreditPlanResponse::from).toList();
    }

    @Cacheable(cacheNames = CacheNames.USER_CREDIT_PLANS)
    @Transactional(readOnly = true)
    public CreditPlanResponse findById(Long id) {
        return repository.findById(id)
                .map(CreditPlanResponse::from)
                .orElseThrow(() -> new NoSuchElementException("User credit plan not found"));
    }

    public CreditPlan findEntityByCode(CreditPlanCode code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("User credit plan not found: " + code));
    }

    public CreditPlan findEntityByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("User credit plan not found: " + code));
    }

    public CreditPlan getDefaultPlan() {
        return repository.findByIsDefaultTrue()
                .orElseGet(() -> repository.findByCode(CreditPlanCode.STANDARD)
                        .orElseThrow(() -> new NoSuchElementException("No default user credit plan configured")));
    }

    public CreditPlanResponse createCustomPlan(CustomCreditPlanRequest request) {
        if (request.assignedCompanyId() == null) {
            throw new IllegalArgumentException("Custom plans must be assigned to a company");
        }
        CreditPlan plan = new CreditPlan();
        String slug = request.displayName() != null
                ? request.displayName().trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "")
                : "CUSTOM";
        plan.setCode("CUSTOM_" + request.assignedCompanyId() + "_" + slug + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        plan.setDisplayName(request.displayName());
        plan.setBasePriceUsd(request.basePriceUsd() != null ? request.basePriceUsd() : java.math.BigDecimal.ZERO);
        plan.setBasePriceNgn(request.basePriceNgn());
        plan.setDescription(request.description());
        plan.setIsDefault(false);
        plan.setIsCompanyPlan(true);
        plan.setSignupRangeLabel(request.signupRangeLabel());
        plan.setServiceLevel(request.serviceLevel() != null ? request.serviceLevel().toUpperCase() : "STANDARD");
        plan.setVisibility(CreditPlanVisibility.CUSTOM.name());
        plan.setAssignedCompanyId(request.assignedCompanyId());
        plan.setPlanCount(request.planCount());
        return CreditPlanResponse.from(repository.save(plan));
    }
}
