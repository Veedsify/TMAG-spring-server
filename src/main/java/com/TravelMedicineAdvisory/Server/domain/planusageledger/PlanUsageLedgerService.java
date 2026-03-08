package com.TravelMedicineAdvisory.Server.domain.planusageledger;

import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanUsageLedgerService {

    private final PlanUsageLedgerRepository repository;
    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;

    public PlanUsageLedgerService(PlanUsageLedgerRepository repository, TravelPlanRepository travelPlanRepository, UserRepository userRepository) {
        this.repository = repository;
        this.travelPlanRepository = travelPlanRepository;
        this.userRepository = userRepository;
    }

    public Page<PlanUsageLedgerResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public PlanUsageLedgerResponse findById(Long id) {
        PlanUsageLedger entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PlanUsageLedger not found"));
        return toResponse(entity);
    }

    public PlanUsageLedgerResponse create(PlanUsageLedgerRequest request) {
        PlanUsageLedger entity = new PlanUsageLedger();
        mapRequestToEntity(request, entity);
        PlanUsageLedger saved = repository.save(entity);
        return toResponse(saved);
    }

    public PlanUsageLedgerResponse update(Long id, PlanUsageLedgerRequest request) {
        PlanUsageLedger entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PlanUsageLedger not found"));
        mapRequestToEntity(request, entity);
        PlanUsageLedger saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("PlanUsageLedger not found");
        }
        repository.deleteById(id);
    }

    private PlanUsageLedgerResponse toResponse(PlanUsageLedger entity) {
        return new PlanUsageLedgerResponse(
            entity.getId(),
            entity.getAction(),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getTravelPlan() != null ? entity.getTravelPlan().getId() : null,
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(PlanUsageLedgerRequest request, PlanUsageLedger entity) {
        entity.setAction(request.action());
        entity.setIpAddress(request.ipAddress());
        entity.setUserAgent(request.userAgent());
        if (request.travelPlanId() != null) {
            TravelPlan travelPlan = travelPlanRepository.findById(request.travelPlanId())
                    .orElseThrow(() -> new NoSuchElementException("TravelPlan not found"));
            entity.setTravelPlan(travelPlan);
        }
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
