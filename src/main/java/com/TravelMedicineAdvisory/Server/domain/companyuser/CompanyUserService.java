package com.TravelMedicineAdvisory.Server.domain.companyuser;

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
public class CompanyUserService {

    private final CompanyUserRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyUserService(CompanyUserRepository repository, CompanyRepository companyRepository, UserRepository userRepository) {
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
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(CompanyUserRequest request, CompanyUser entity) {
        entity.setRole(request.role());
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
