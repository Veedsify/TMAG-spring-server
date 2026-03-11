package com.TravelMedicineAdvisory.Server.domain.companyuser;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyUserService {

    private final CompanyUserRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyUserService(CompanyUserRepository repository, CompanyRepository companyRepository,
            UserRepository userRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Page<CompanyUserResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public CompanyUserResponse findById(Long id) {
        CompanyUser entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CompanyUser not found"));
        return toResponse(entity);
    }

    public CompanyUserResponse create(CompanyUserRequest request) {
        CompanyUser entity = new CompanyUser();
        mapRequestToEntity(request, entity);
        CompanyUser saved = repository.save(entity);
        return toResponse(saved);
    }

    public CompanyUserResponse update(Long id, CompanyUserRequest request) {
        CompanyUser entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CompanyUser not found"));
        mapRequestToEntity(request, entity);
        CompanyUser saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("CompanyUser not found");
        }
        repository.deleteById(id);
    }

    private CompanyUserResponse toResponse(CompanyUser entity) {
        return new CompanyUserResponse(
                entity.getId(),
                entity.getRole(),
                entity.getCompany() != null ? entity.getCompany().getId() : null,
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public List<Map<String, Object>> findMyCompanies(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return repository.findAllByUser(user).stream()
                .filter(cu -> cu.getCompany() != null)
                .map(cu -> {
                    Company c = cu.getCompany();
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId());
                    m.put("name", c.getName());
                    m.put("industry", c.getIndustry());
                    m.put("plan", c.getPlan());
                    m.put("company_code", c.getCompanyCode());
                    m.put("total_credits", c.getTotalCredits());
                    m.put("used_credits", c.getUsedCredits());
                    m.put("employee_count", c.getEmployeeCount());
                    m.put("billing_currency", c.getBillingCurrency());
                    m.put("role", cu.getRole());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(CompanyUserRequest request, CompanyUser entity) {
        entity.setRole(request.role());
        if (request.company_id() != null) {
            Company company = companyRepository.findById(request.company_id())
                    .orElseThrow(() -> new NoSuchElementException("Company not found"));
            entity.setCompany(company);
        }
        if (request.user_id() != null) {
            User user = userRepository.findById(request.user_id())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
