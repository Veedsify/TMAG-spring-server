package com.TravelMedicineAdvisory.Server.domain.faqitem;

import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FaqItemService {

    private final FaqItemRepository repository;

    public FaqItemService(FaqItemRepository repository) {
        this.repository = repository;
    }

    public Page<FaqItemResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public FaqItemResponse findById(Long id) {
        FaqItem entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("FaqItem not found"));
        return toResponse(entity);
    }

    public FaqItemResponse create(FaqItemRequest request) {
        FaqItem entity = new FaqItem();
        mapRequestToEntity(request, entity);
        FaqItem saved = repository.save(entity);
        return toResponse(saved);
    }

    public FaqItemResponse update(Long id, FaqItemRequest request) {
        FaqItem entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("FaqItem not found"));
        mapRequestToEntity(request, entity);
        FaqItem saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("FaqItem not found");
        }
        repository.deleteById(id);
    }

    private FaqItemResponse toResponse(FaqItem entity) {
        return new FaqItemResponse(
            entity.getId(),
            entity.getQuestion(),
            entity.getAnswer(),
            entity.getCategory(),
            entity.getPosition(),
            entity.getActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(FaqItemRequest request, FaqItem entity) {
        entity.setQuestion(request.question());
        entity.setAnswer(request.answer());
        entity.setCategory(request.category());
        entity.setPosition(request.position());
        entity.setActive(request.isActive());
    }
}
