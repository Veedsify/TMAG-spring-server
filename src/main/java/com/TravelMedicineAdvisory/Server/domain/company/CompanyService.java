package com.TravelMedicineAdvisory.Server.domain.company;

import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

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

    public CompanyService(CompanyRepository repository, CreditRepository creditRepository, RandomNumberGenerator randomNumberGenerator) {
        this.repository = repository;
        this.creditRepository = creditRepository;
        this.randomNumberGenerator = randomNumberGenerator;
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
        String companyCode = "TMA-" + randomNumberGenerator.generateNumber();

        if (repository.findByCompanyCode(companyCode).isPresent()) {
            companyCode = "TMA-" + randomNumberGenerator.generateNumber();
        }

        entity.setCompanyCode(companyCode);
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

    public boolean validateCompanyCode(String code) {
        return repository.findByCompanyCode(code).isPresent();
    }

    public CompanyResponse purchaseCredits(Long id, Integer amount, String reference) {
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
                entity.getBillingCurrency(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private void mapRequestToEntity(CompanyRequest request, Company entity) {
        entity.setName(request.name());
        entity.setIndustry(request.industry());
        entity.setTotalCredits(request.totalCredits());
        entity.setUsedCredits(request.usedCredits());
        entity.setEmployeeCount(request.employeeCount());
        entity.setPlan(request.plan());
        entity.setCompanyCode(request.companyCode());
        if (request.billingCurrency() != null) {
            entity.setBillingCurrency(request.billingCurrency());
        }
    }
}
