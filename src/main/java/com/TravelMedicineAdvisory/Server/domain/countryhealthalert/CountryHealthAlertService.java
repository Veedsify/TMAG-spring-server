package com.TravelMedicineAdvisory.Server.domain.countryhealthalert;

import com.TravelMedicineAdvisory.Server.domain.country.Country;
import com.TravelMedicineAdvisory.Server.domain.country.CountryRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CountryHealthAlertService {

    private final CountryHealthAlertRepository repository;
    private final CountryRepository countryRepository;

    public CountryHealthAlertService(CountryHealthAlertRepository repository, CountryRepository countryRepository) {
        this.repository = repository;
        this.countryRepository = countryRepository;
    }

    public Page<CountryHealthAlertResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public CountryHealthAlertResponse findById(Long id) {
        CountryHealthAlert entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CountryHealthAlert not found"));
        return toResponse(entity);
    }

    public CountryHealthAlertResponse create(CountryHealthAlertRequest request) {
        CountryHealthAlert entity = new CountryHealthAlert();
        mapRequestToEntity(request, entity);
        CountryHealthAlert saved = repository.save(entity);
        return toResponse(saved);
    }

    public CountryHealthAlertResponse update(Long id, CountryHealthAlertRequest request) {
        CountryHealthAlert entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CountryHealthAlert not found"));
        mapRequestToEntity(request, entity);
        CountryHealthAlert saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("CountryHealthAlert not found");
        }
        repository.deleteById(id);
    }

    private CountryHealthAlertResponse toResponse(CountryHealthAlert entity) {
        return new CountryHealthAlertResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getSeverity(),
            entity.getAlertType(),
            entity.getSource(),
            entity.getSourceUrl(),
            entity.getStartsAt(),
            entity.getExpiresAt(),
            entity.getActive(),
            entity.getCountry() != null ? entity.getCountry().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(CountryHealthAlertRequest request, CountryHealthAlert entity) {
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setSeverity(request.severity());
        entity.setAlertType(request.alertType());
        entity.setSource(request.source());
        entity.setSourceUrl(request.sourceUrl());
        entity.setStartsAt(request.startsAt());
        entity.setExpiresAt(request.expiresAt());
        entity.setActive(request.isActive());
        if (request.countryId() != null) {
            Country country = countryRepository.findById(request.countryId())
                    .orElseThrow(() -> new NoSuchElementException("Country not found"));
            entity.setCountry(country);
        }
    }
}
