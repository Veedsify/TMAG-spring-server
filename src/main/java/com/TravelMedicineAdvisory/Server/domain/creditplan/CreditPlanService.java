package com.TravelMedicineAdvisory.Server.domain.creditplan;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CreditPlanService {

    private final CreditPlanRepository repository;

    public CreditPlanService(CreditPlanRepository repository) {
        this.repository = repository;
    }

    public List<CreditPlanResponse> findAll() {
        return repository.findAll().stream()
                .map(CreditPlanResponse::from)
                .toList();
    }

    public CreditPlanResponse findById(Long id) {
        return repository.findById(id)
                .map(CreditPlanResponse::from)
                .orElseThrow(() -> new NoSuchElementException("User credit plan not found"));
    }

    public CreditPlan findEntityByCode(CreditPlanCode code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("User credit plan not found: " + code));
    }

    public CreditPlan getDefaultPlan() {
        return repository.findByIsDefaultTrue()
                .orElseGet(() -> repository.findByCode(CreditPlanCode.STANDARD)
                        .orElseThrow(() -> new NoSuchElementException("No default user credit plan configured")));
    }
}
