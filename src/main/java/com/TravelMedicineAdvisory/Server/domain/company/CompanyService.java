package com.TravelMedicineAdvisory.Server.domain.company;

import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository repository;

    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    public Page<CompanyResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public CompanyResponse findById(Long id) {
        Company entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        return toResponse(entity);
    }

    public CompanyResponse create(CompanyRequest request) {
        Company entity = new Company();
        mapRequestToEntity(request, entity);
        Company saved = repository.save(entity);
        return toResponse(saved);
    }

    public CompanyResponse update(Long id, CompanyRequest request) {
        Company entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        mapRequestToEntity(request, entity);
        Company saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Company not found");
        }
        repository.deleteById(id);
    }

    private CompanyResponse toResponse(Company entity) {
        return new CompanyResponse(
            entity.getId(),
            entity.getName(),
            entity.getIndustry(),
            entity.getTotalCredits(),
            entity.getUsedCredits(),
            entity.getEmployeeCount(),
            entity.getPlan(),
            entity.getCompanyCode(),
            entity.getLogo() != null ? entity.getLogo().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(CompanyRequest request, Company entity) {
        entity.setName(request.name());
        entity.setIndustry(request.industry());
        entity.setTotalCredits(request.totalCredits());
        entity.setUsedCredits(request.usedCredits());
        entity.setEmployeeCount(request.employeeCount());
        entity.setPlan(request.plan());
        entity.setCompanyCode(request.companyCode());
    }
}
