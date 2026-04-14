package com.TravelMedicineAdvisory.Server.domain.company;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.utils.RandomNumberGenerator;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository repository;
    private final CreditRepository creditRepository;
    private final RandomNumberGenerator randomNumberGenerator;

    public CompanyService(CompanyRepository repository, CreditRepository creditRepository,
            RandomNumberGenerator randomNumberGenerator) {
        this.repository = repository;
        this.creditRepository = creditRepository;
        this.randomNumberGenerator = randomNumberGenerator;
    }

    public Page<CompanyResponse> findAll(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public CompanyResponse findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        Company entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        return toResponse(entity);
    }

    public CompanyResponse create(CompanyRequest request) {
        Company entity = new Company();
        mapRequestToEntity(request, entity);
        String companyCode = "TMA-" + randomNumberGenerator.generateNumber();

        if (repository.findByCompanyCode(companyCode).isPresent()) {
            companyCode = "TMA-" + randomNumberGenerator.generateNumber();
        }

        entity.setCompanyCode(companyCode);
        Company saved = repository.save(entity);
        return toResponse(saved);
    }

    public CompanyResponse update(Long id, CompanyRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        Company entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        mapRequestToEntity(request, entity);

        if (entity != null) {
            Company saved = repository.save(entity);
            return toResponse(saved);
        }

        throw new IllegalArgumentException("Invalid company entity");
    }

    public boolean validateCompanyCode(String code) {
        return repository.findByCompanyCode(code).isPresent();
    }

    public CompanyResponse purchaseCredits(Long id, Integer amount, String reference) {
        if (id == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        Company company = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));
        int newTotal = (company.getTotalCredits() != null ? company.getTotalCredits() : 0) + amount;
        company.setTotalCredits(newTotal);
        repository.save(company);

        Credit credit = new Credit();
        credit.setCompany(company);
        credit.setAmount(amount);
        credit.setType("purchase");
        credit.setReference(reference != null ? reference : "Credit purchase");
        credit.setBalanceAfter(newTotal);
        creditRepository.save(credit);

        return toResponse(company);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Company not found");
        }
        repository.deleteById(id);
    }

    private CompanyResponse toResponse(Company entity) {
        String resolvedPlan = entity.getCreditPlan() != null && entity.getCreditPlan().getCode() != null
                ? entity.getCreditPlan().getCode().name()
                : entity.getPlan();
        return new CompanyResponse(
                entity.getId(),
                entity.getName(),
                entity.getIndustry(),
                entity.getTotalCredits(),
                entity.getUsedCredits(),
                entity.getEmployeeCount(),
                resolvedPlan,
                entity.getCompanyCode(),
                entity.getLogo() != null ? entity.getLogo().getId() : null,
                entity.getBillingCurrency(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreditPlan() != null
                        ? com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanResponse.from(entity.getCreditPlan())
                        : null);
    }

    private void mapRequestToEntity(CompanyRequest request, Company entity) {
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.industry() != null) {
            entity.setIndustry(request.industry());
        }
        if (request.totalCredits() != null) {
            entity.setTotalCredits(request.totalCredits());
        }
        if (request.usedCredits() != null) {
            entity.setUsedCredits(request.usedCredits());
        }
        if (request.employeeCount() != null) {
            entity.setEmployeeCount(request.employeeCount());
        }
        if (request.plan() != null) {
            entity.setPlan(request.plan());
        }
        if (request.companyCode() != null) {
            entity.setCompanyCode(request.companyCode());
        }
        if (request.billingCurrency() != null) {
            entity.setBillingCurrency(request.billingCurrency());
        }
    }
}
