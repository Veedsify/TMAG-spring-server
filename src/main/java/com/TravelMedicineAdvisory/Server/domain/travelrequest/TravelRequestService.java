package com.TravelMedicineAdvisory.Server.domain.travelrequest;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TravelRequestService {

    private final TravelRequestRepository repository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    public TravelRequestService(TravelRequestRepository repository, CompanyRepository companyRepository, EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
    }

    public Page<TravelRequestResponse> findAll(Long companyId, Pageable pageable) {
        if (companyId != null) {
            return repository.findAllByCompanyId(companyId, pageable)
                    .map(this::toResponse);
        }
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public TravelRequestResponse findById(Long id) {
        TravelRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelRequest not found"));
        return toResponse(entity);
    }

    public TravelRequestResponse create(TravelRequestRequest request) {
        TravelRequest entity = new TravelRequest();
        mapRequestToEntity(request, entity);
        TravelRequest saved = repository.save(entity);
        return toResponse(saved);
    }

    public TravelRequestResponse update(Long id, TravelRequestRequest request) {
        TravelRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelRequest not found"));
        mapRequestToEntity(request, entity);
        TravelRequest saved = repository.save(entity);
        return toResponse(saved);
    }

    public TravelRequestResponse approve(Long id) {
        TravelRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelRequest not found"));
        entity.setStatus("approved");
        return toResponse(repository.save(entity));
    }

    public TravelRequestResponse reject(Long id) {
        TravelRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelRequest not found"));
        entity.setStatus("rejected");
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("TravelRequest not found");
        }
        repository.deleteById(id);
    }

    private TravelRequestResponse toResponse(TravelRequest entity) {
        return new TravelRequestResponse(
            entity.getId(),
            entity.getDestination(),
            entity.getDates(),
            entity.getStatus(),
            entity.getSubmittedAt(),
            entity.getCompany() != null ? entity.getCompany().getId() : null,
            entity.getEmployee() != null ? entity.getEmployee().getId() : null,
            entity.getEmployee() != null ? entity.getEmployee().getName() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(TravelRequestRequest request, TravelRequest entity) {
        entity.setDestination(request.destination());
        entity.setDates(request.dates());
        entity.setStatus(request.status());
        entity.setSubmittedAt(request.submittedAt());
        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new NoSuchElementException("Company not found"));
            entity.setCompany(company);
        }
        if (request.employeeId() != null) {
            Employee employee = employeeRepository.findById(request.employeeId())
                    .orElseThrow(() -> new NoSuchElementException("Employee not found"));
            entity.setEmployee(employee);
        }
    }
}
