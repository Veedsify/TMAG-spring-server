package com.TravelMedicineAdvisory.Server.domain.countryaccommodation;

import com.TravelMedicineAdvisory.Server.domain.country.Country;
import com.TravelMedicineAdvisory.Server.domain.country.CountryRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CountryAccommodationService {

    private final CountryAccommodationRepository repository;
    private final CountryRepository countryRepository;

    public CountryAccommodationService(CountryAccommodationRepository repository, CountryRepository countryRepository) {
        this.repository = repository;
        this.countryRepository = countryRepository;
    }

    public Page<CountryAccommodationResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public CountryAccommodationResponse findById(Long id) {
        CountryAccommodation entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CountryAccommodation not found"));
        return toResponse(entity);
    }

    public CountryAccommodationResponse create(CountryAccommodationRequest request) {
        CountryAccommodation entity = new CountryAccommodation();
        mapRequestToEntity(request, entity);
        CountryAccommodation saved = repository.save(entity);
        return toResponse(saved);
    }

    public CountryAccommodationResponse update(Long id, CountryAccommodationRequest request) {
        CountryAccommodation entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CountryAccommodation not found"));
        mapRequestToEntity(request, entity);
        CountryAccommodation saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("CountryAccommodation not found");
        }
        repository.deleteById(id);
    }

    private CountryAccommodationResponse toResponse(CountryAccommodation entity) {
        return new CountryAccommodationResponse(
            entity.getId(),
            entity.getCity(),
            entity.getType(),
            entity.getName(),
            entity.getAvgPricePerNight(),
            entity.getCurrency(),
            entity.getRating(),
            entity.getSourceUrl(),
            entity.getLastUpdatedAt(),
            entity.getCountry() != null ? entity.getCountry().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(CountryAccommodationRequest request, CountryAccommodation entity) {
        entity.setCity(request.city());
        entity.setType(request.type());
        entity.setName(request.name());
        entity.setAvgPricePerNight(request.avgPricePerNight());
        entity.setCurrency(request.currency());
        entity.setRating(request.rating());
        entity.setSourceUrl(request.sourceUrl());
        entity.setLastUpdatedAt(request.lastUpdatedAt());
        if (request.countryId() != null) {
            Country country = countryRepository.findById(request.countryId())
                    .orElseThrow(() -> new NoSuchElementException("Country not found"));
            entity.setCountry(country);
        }
    }
}
