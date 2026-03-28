package com.TravelMedicineAdvisory.Server.domain.creditrequest;

import com.TravelMedicineAdvisory.Server.core.notifications.AdminNotificationService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreditRequestService {

    private final CreditRequestRepository repository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final QueueService queueService;
    private final AdminNotificationService adminNotificationService;

    public CreditRequestService(CreditRequestRepository repository, CompanyRepository companyRepository, 
            EmployeeRepository employeeRepository, UserRepository userRepository, QueueService queueService,
            AdminNotificationService adminNotificationService) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.queueService = queueService;
        this.adminNotificationService = adminNotificationService;
    }

    public Page<CreditRequestResponse> findAll(Long companyId, Pageable pageable) {
        if (companyId != null) {
            return repository.findAllByCompanyId(companyId, pageable)
                    .map(this::toResponse);
        }
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public CreditRequestResponse findById(Long id) {
        CreditRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CreditRequest not found"));
        return toResponse(entity);
    }

    public CreditRequestResponse create(CreditRequestRequest request) {
        CreditRequest entity = new CreditRequest();
        mapRequestToEntity(request, entity);
        CreditRequest saved = repository.save(entity);

        sendNewRequestNotificationToHr(entity);

        return toResponse(saved);
    }

    private void sendNewRequestNotificationToHr(CreditRequest request) {
        if (request.getCompany() == null || request.getEmployee() == null) return;

        String employeeName = request.getEmployee().getName() != null 
                ? request.getEmployee().getName() 
                : "Employee";
        
        adminNotificationService.notifyCompanyAdmins(
                request.getCompany().getId(),
                "New credit request from " + employeeName,
                JobType.EMAIL_CREDIT_REQUEST_SUBMITTED,
                Map.of(
                        "employeeName", employeeName,
                        "credits", String.valueOf(request.getCreditsRequested()))
        );
    }

    public CreditRequestResponse update(Long id, CreditRequestRequest request) {
        CreditRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CreditRequest not found"));
        mapRequestToEntity(request, entity);
        CreditRequest saved = repository.save(entity);
        return toResponse(saved);
    }

    public CreditRequestResponse approve(Long id) {
        CreditRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CreditRequest not found"));
        entity.setStatus("approved");
        CreditRequest saved = repository.save(entity);

        sendStatusEmail(entity, true, null);

        return toResponse(saved);
    }

    public CreditRequestResponse reject(Long id, String reason) {
        CreditRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("CreditRequest not found"));
        entity.setStatus("rejected");
        CreditRequest saved = repository.save(entity);

        sendStatusEmail(entity, false, reason);

        return toResponse(saved);
    }

    private void sendStatusEmail(CreditRequest entity, boolean approved, String reason) {
        if (entity.getEmployee() != null && entity.getEmployee().getUser() != null) {
            User user = entity.getEmployee().getUser();
            String firstName = user.getFirstName() != null ? user.getFirstName() : "there";
            String companyName = entity.getCompany() != null ? entity.getCompany().getName() : "your company";

            if (approved) {
                queueService.dispatch(JobType.EMAIL_CREDIT_REQUEST_APPROVED, Map.of(
                        "to", user.getEmail(),
                        "subject", "Your credit request has been approved",
                        "variables", Map.of(
                                "firstName", firstName,
                                "credits", String.valueOf(entity.getCreditsRequested()),
                                "companyName", companyName)));
            } else {
                queueService.dispatch(JobType.EMAIL_CREDIT_REQUEST_REJECTED, Map.of(
                        "to", user.getEmail(),
                        "subject", "Update on your credit request",
                        "variables", Map.of(
                                "firstName", firstName,
                                "credits", String.valueOf(entity.getCreditsRequested()),
                                "reason", reason != null ? reason : "Not specified",
                                "companyName", companyName)));
            }
        }
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("CreditRequest not found");
        }
        repository.deleteById(id);
    }

    private CreditRequestResponse toResponse(CreditRequest entity) {
        return new CreditRequestResponse(
            entity.getId(),
            entity.getCreditsRequested(),
            entity.getReason(),
            entity.getStatus(),
            entity.getSubmittedAt(),
            entity.getCompany() != null ? entity.getCompany().getId() : null,
            entity.getEmployee() != null ? entity.getEmployee().getId() : null,
            entity.getEmployee() != null ? entity.getEmployee().getName() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(CreditRequestRequest request, CreditRequest entity) {
        entity.setCreditsRequested(request.creditsRequested());
        entity.setReason(request.reason());
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
