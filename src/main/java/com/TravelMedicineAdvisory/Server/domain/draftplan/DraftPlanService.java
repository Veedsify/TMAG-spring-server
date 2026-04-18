package com.TravelMedicineAdvisory.Server.domain.draftplan;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class DraftPlanService {

    private final DraftPlanRepository repository;
    private final UserRepository userRepository;

    public DraftPlanService(DraftPlanRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DraftPlanResponse> findByUserId(Long userId) {
        return repository.findActiveByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DraftPlanResponse findById(Long id, Long userId) {
        DraftPlan draft = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Draft plan not found"));
        if (draft.getUser() == null || !draft.getUser().getId().equals(userId)) {
            throw new NoSuchElementException("Draft plan not found");
        }
        return toResponse(draft);
    }

    public DraftPlanResponse create(Long userId, SaveDraftPlanRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        DraftPlan draft = new DraftPlan();
        draft.setUser(user);
        applyRequest(draft, request);
        repository.save(draft);

        return toResponse(draft);
    }

    public DraftPlanResponse update(Long id, Long userId, SaveDraftPlanRequest request) {
        DraftPlan draft = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Draft plan not found"));
        if (draft.getUser() == null || !draft.getUser().getId().equals(userId)) {
            throw new NoSuchElementException("Draft plan not found");
        }
        applyRequest(draft, request);
        repository.save(draft);
        return toResponse(draft);
    }

    public void delete(Long id, Long userId) {
        DraftPlan draft = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Draft plan not found"));
        if (draft.getUser() == null || !draft.getUser().getId().equals(userId)) {
            throw new NoSuchElementException("Draft plan not found");
        }
        repository.deleteById(id);
    }

    private void applyRequest(DraftPlan draft, SaveDraftPlanRequest request) {
        draft.setCountry(request.country());
        draft.setAnswersJson(request.answersJson());
        draft.setCategoryIndex(request.categoryIndex());
        draft.setShowVerify(request.showVerify() != null ? request.showVerify() : Boolean.FALSE);
        draft.setShowIntro(request.showIntro() != null ? request.showIntro() : Boolean.TRUE);
        draft.setRiskConsentGiven(request.riskConsentGiven() != null ? request.riskConsentGiven() : Boolean.FALSE);
        draft.setTitle(buildTitle(request.country(), draft.getUpdatedAt()));
    }

    private static String buildTitle(String country, LocalDateTime dateTime) {
        String countryPart = (country != null && !country.isBlank()) ? country : "Untitled";
        if (dateTime != null) {
            return "Draft — %s — %s".formatted(countryPart, dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        }
        return "Draft — %s".formatted(countryPart);
    }

    private DraftPlanResponse toResponse(DraftPlan entity) {
        return new DraftPlanResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getCountry(),
                entity.getAnswersJson(),
                entity.getCategoryIndex() != null ? entity.getCategoryIndex() : 0,
                entity.getShowVerify() != null ? entity.getShowVerify() : Boolean.FALSE,
                entity.getShowIntro() != null ? entity.getShowIntro() : Boolean.TRUE,
                entity.getRiskConsentGiven() != null ? entity.getRiskConsentGiven() : Boolean.FALSE,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
