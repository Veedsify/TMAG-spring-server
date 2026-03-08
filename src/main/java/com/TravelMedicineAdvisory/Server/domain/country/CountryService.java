package com.TravelMedicineAdvisory.Server.domain.country;

import java.util.List;
import java.util.NoSuchElementException;

import com.TravelMedicineAdvisory.Server.core.cache.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CountryService {

    private final CountryRepository repository;
    private final CacheConfig cacheConfig;

    public CountryService(CountryRepository repository) {
        this.repository = repository;
        this.cacheConfig = new CacheConfig();
    }

    public Page<CountryResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public CountryResponse findById(Long id) {
        Country entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Country not found"));
        return toResponse(entity);
    }

    public CountryResponse create(CountryRequest request) {
        Country entity = new Country();
        mapRequestToEntity(request, entity);
        Country saved = repository.save(entity);
        return toResponse(saved);
    }

    public CountryResponse update(Long id, CountryRequest request) {
        Country entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Country not found"));
        mapRequestToEntity(request, entity);
        Country saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Country not found");
        }
        repository.deleteById(id);
    }

    public List<CountryResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private CountryResponse toResponse(Country entity) {
        return new CountryResponse(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getRegion(),
                entity.getContinent(),
                entity.getRiskLevel(),
                entity.getVisaInfo(),
                entity.getCurrency(),
                entity.getLanguage(),
                entity.getTimezone(),
                entity.getHealthAdvisory(),
                entity.getTravelAdvisory(),
                entity.getEmergencyNumber(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(CountryRequest request, Country entity) {
        entity.setName(request.name());
        entity.setCode(request.code());
        entity.setRegion(request.region());
        entity.setContinent(request.continent());
        entity.setRiskLevel(request.riskLevel());
        entity.setVisaInfo(request.visaInfo());
        entity.setCurrency(request.currency());
        entity.setLanguage(request.language());
        entity.setTimezone(request.timezone());
        entity.setHealthAdvisory(request.healthAdvisory());
        entity.setTravelAdvisory(request.travelAdvisory());
        entity.setEmergencyNumber(request.emergencyNumber());
        entity.setActive(request.isActive());
    }
}
