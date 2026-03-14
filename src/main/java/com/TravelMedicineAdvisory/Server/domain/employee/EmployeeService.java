package com.TravelMedicineAdvisory.Server.domain.employee;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public EmployeeService(EmployeeRepository repository, CompanyRepository companyRepository, UserRepository userRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Page<EmployeeResponse> findAll(Long companyId, Pageable pageable) {
        if (companyId != null) {
            return repository.findAllByCompanyId(companyId, pageable)
                    .map(this::toResponse);
        }
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public EmployeeResponse findById(Long id) {
        Employee entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        return toResponse(entity);
    }

    public EmployeeResponse create(EmployeeRequest request) {
        Employee entity = new Employee();
        mapRequestToEntity(request, entity);
        Employee saved = repository.save(entity);
        return toResponse(saved);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        mapRequestToEntity(request, entity);
        Employee saved = repository.save(entity);
        return toResponse(saved);
    }

    public EmployeeResponse allocateCredits(Long id, Integer creditsAllocated) {
        Employee entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        entity.setCreditsAllocated(creditsAllocated);
        return toResponse(repository.save(entity));
    }

    public EmployeeResponse updateStatus(Long id, String status) {
        Employee entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        entity.setStatus(status);
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Employee not found");
        }
        repository.deleteById(id);
    }

    private EmployeeResponse toResponse(Employee entity) {
        return new EmployeeResponse(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getDepartment(),
            entity.getCreditsUsed(),
            entity.getCreditsAllocated(),
            entity.getStatus(),
            entity.getPlansGenerated(),
            entity.getCompany() != null ? entity.getCompany().getId() : null,
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(EmployeeRequest request, Employee entity) {
        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setDepartment(request.department());
        entity.setCreditsUsed(request.creditsUsed());
        entity.setCreditsAllocated(request.creditsAllocated());
        entity.setStatus(request.status());
        entity.setPlansGenerated(request.plansGenerated());
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
