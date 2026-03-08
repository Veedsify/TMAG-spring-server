package com.TravelMedicineAdvisory.Server.domain.pricingplan;

import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PricingPlanService {

    private final PricingPlanRepository repository;

    public PricingPlanService(PricingPlanRepository repository) {
        this.repository = repository;
    }

    public Page<PricingPlanResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public PricingPlanResponse findById(Long id) {
        PricingPlan entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PricingPlan not found"));
        return toResponse(entity);
    }

    public PricingPlanResponse create(PricingPlanRequest request) {
        PricingPlan entity = new PricingPlan();
        mapRequestToEntity(request, entity);
        PricingPlan saved = repository.save(entity);
        return toResponse(saved);
    }

    public PricingPlanResponse update(Long id, PricingPlanRequest request) {
        PricingPlan entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PricingPlan not found"));
        mapRequestToEntity(request, entity);
        PricingPlan saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("PricingPlan not found");
        }
        repository.deleteById(id);
    }

    private PricingPlanResponse toResponse(PricingPlan entity) {
        return new PricingPlanResponse(
            entity.getId(),
            entity.getName(),
            entity.getPrice(),
            entity.getPeriod(),
            entity.getDescription(),
            entity.getFeatures(),
            entity.getCreditsIncluded(),
            entity.getPosition(),
            entity.getActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(PricingPlanRequest request, PricingPlan entity) {
        entity.setName(request.name());
        entity.setPrice(request.price());
        entity.setPeriod(request.period());
        entity.setDescription(request.description());
        entity.setFeatures(request.features());
        entity.setCreditsIncluded(request.creditsIncluded());
        entity.setPosition(request.position());
        entity.setActive(request.isActive());
    }
}
