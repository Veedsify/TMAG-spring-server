package com.TravelMedicineAdvisory.Server.domain.translation;

import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;

@Service
@Transactional
public class TranslationService {

    private final TranslationRepository repository;

    public TranslationService(TranslationRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = CacheNames.TRANSLATIONS)
    @Transactional(readOnly = true)
    public Page<TranslationResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    @Cacheable(cacheNames = CacheNames.TRANSLATIONS)
    @Transactional(readOnly = true)
    public TranslationResponse findById(Long id) {
        Translation entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Translation not found"));
        return toResponse(entity);
    }

    @CacheEvict(cacheNames = CacheNames.TRANSLATIONS, allEntries = true)
    public TranslationResponse create(TranslationRequest request) {
        Translation entity = new Translation();
        mapRequestToEntity(request, entity);
        Translation saved = repository.save(entity);
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheNames.TRANSLATIONS, allEntries = true)
    public TranslationResponse update(Long id, TranslationRequest request) {
        Translation entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Translation not found"));
        mapRequestToEntity(request, entity);
        Translation saved = repository.save(entity);
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheNames.TRANSLATIONS, allEntries = true)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Translation not found");
        }
        repository.deleteById(id);
    }

    private TranslationResponse toResponse(Translation entity) {
        return new TranslationResponse(
            entity.getId(),
            entity.getKey(),
            entity.getValue(),
            entity.getModel(),
            entity.getModelId(),
            entity.getLanguage(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(TranslationRequest request, Translation entity) {
        entity.setKey(request.key());
        entity.setValue(request.value());
        entity.setModel(request.model());
        entity.setModelId(request.modelId());
        entity.setLanguage(request.language());
    }
}
