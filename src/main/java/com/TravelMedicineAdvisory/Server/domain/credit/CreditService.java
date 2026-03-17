package com.TravelMedicineAdvisory.Server.domain.credit;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class CreditService {

    private final CreditRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CreditService(CreditRepository repository, CompanyRepository companyRepository, UserRepository userRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Page<CreditResponse> findAll(Long companyId, Pageable pageable) {
        if (companyId != null) {
            return repository.findAllByCompanyId(companyId, pageable)
                    .map(this::toResponse);
        }

        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public CreditResponse findById(Long id) {
        Credit entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Credit not found"));
        return toResponse(entity);
    }

    public CreditResponse create(CreditRequest request) {
        Credit entity = new Credit();
        mapRequestToEntity(request, entity);
        Credit saved = repository.save(entity);
        return toResponse(saved);
    }

    public CreditResponse update(Long id, CreditRequest request) {
        Credit entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Credit not found"));
        mapRequestToEntity(request, entity);
        Credit saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Credit not found");
        }
        repository.deleteById(id);
    }

    private CreditResponse toResponse(Credit entity) {
        return new CreditResponse(
            entity.getId(),
            entity.getAmount(),
            entity.getType(),
            entity.getReference(),
            entity.getBalanceAfter(),
            entity.getCompany() != null ? entity.getCompany().getId() : null,
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(CreditRequest request, Credit entity) {
        entity.setAmount(request.amount());
        entity.setType(request.type());
        entity.setReference(request.reference());
        entity.setBalanceAfter(request.balanceAfter());
        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new NoSuchElementException("Company not found"));
            entity.setCompany(company);
        }
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
