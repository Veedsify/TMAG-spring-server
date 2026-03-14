package com.TravelMedicineAdvisory.Server.domain.employee;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final QueueService queueService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmployeeService(EmployeeRepository repository, CompanyRepository companyRepository,
            UserRepository userRepository, CompanyUserRepository companyUserRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            QueueService queueService) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.queueService = queueService;
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

    public EmployeeResponse invite(EmployeeInviteRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        // Check if user already exists with this email
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        Role role = roleRepository.findByName("Individual").orElse(null);

        // Split name into first/last
        String[] nameParts = request.name().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Generate invitation token
        String invitationToken = generateToken(32);

        // Create User with temporary password
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setName(request.name());
        user.setUsername(request.email()); // use email as username
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(invitationToken)); // temp password
        user.setOnboardingStage(0);
        user.setOnboarded(false);
        user.setVerified(false);
        user.setMustChangePassword(true);
        user.setInvitationToken(invitationToken);
        user.setInvitationTokenExpiry(LocalDateTime.now().plusDays(7));
        user.setType("COMPANY");
        user.setCredits(0);
        user.setRole(role);
        User savedUser = userRepository.save(user);

        // Create Employee linked to User and Company
        Employee employee = new Employee();
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setDepartment(request.department());
        employee.setCreditsAllocated(request.creditsAllocated() != null ? request.creditsAllocated() : 0);
        employee.setCreditsUsed(0);
        employee.setPlansGenerated(0);
        employee.setStatus("active");
        employee.setCompany(company);
        employee.setUser(savedUser);
        Employee savedEmployee = repository.save(employee);

        // Create CompanyUser link
        CompanyUser companyUser = new CompanyUser();
        companyUser.setRole("Individual");
        companyUser.setCompany(company);
        companyUser.setUser(savedUser);
        companyUserRepository.save(companyUser);

        // Queue invitation email
        String inviteLink = frontendUrl + "/accept-invitation?token=" + invitationToken;
        queueService.dispatch(JobType.EMAIL_EMPLOYEE_INVITATION, Map.of(
                "to", request.email(),
                "subject", "You're invited to join " + company.getName() + " on TMAG",
                "variables", Map.of(
                        "firstName", firstName,
                        "companyName", company.getName(),
                        "link", inviteLink)));

        return toResponse(savedEmployee);
    }

    private String generateToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
