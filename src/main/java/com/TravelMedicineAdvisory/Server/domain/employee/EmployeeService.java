package com.TravelMedicineAdvisory.Server.domain.employee;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
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
    private final CreditRepository creditRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmployeeService(EmployeeRepository repository, CompanyRepository companyRepository,
            UserRepository userRepository, CompanyUserRepository companyUserRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            QueueService queueService, CreditRepository creditRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.queueService = queueService;
        this.creditRepository = creditRepository;
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

        // Sync credits to CompanyUser as well
        if (entity.getUser() != null) {
            companyUserRepository.findByUser(entity.getUser())
                    .ifPresent(cu -> {
                        cu.setCreditsAllocated(creditsAllocated);
                        companyUserRepository.save(cu);
                    });
        }

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

        int creditsToAllocate = request.creditsAllocated() != null ? request.creditsAllocated() : 0;

        // Validate company has enough available credits
        int totalCredits = company.getTotalCredits() != null ? company.getTotalCredits() : 0;
        int usedCredits = company.getUsedCredits() != null ? company.getUsedCredits() : 0;
        int availableCredits = totalCredits - usedCredits;
        if (creditsToAllocate > availableCredits) {
            throw new IllegalArgumentException(
                "Insufficient company credits. Available: " + availableCredits + ", requested: " + creditsToAllocate);
        }

        // Map frontend role name to Role entity name (e.g. "HR" → "Hr")
        String roleName = normalizeRoleName(request.role());
        Role role = roleRepository.findByName(roleName).orElse(
                roleRepository.findByName("Individual").orElse(null));

        // Split name into first/last
        String[] nameParts = request.name().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Generate invitation token
        String invitationToken = generateToken(32);

        // Create User with credits pre-allocated
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
        user.setCredits(creditsToAllocate);
        user.setRole(role);
        User savedUser = userRepository.save(user);

        // Create Employee linked to User and Company
        Employee employee = new Employee();
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setDepartment(request.department());
        employee.setCreditsAllocated(creditsToAllocate);
        employee.setCreditsUsed(0);
        employee.setPlansGenerated(0);
        employee.setStatus("active");
        employee.setCompany(company);
        employee.setUser(savedUser);
        Employee savedEmployee = repository.save(employee);

        // Create CompanyUser link
        String assignedRole = request.role() != null && !request.role().isBlank()
                ? request.role() : "Individual";
        CompanyUser companyUser = new CompanyUser();
        companyUser.setRole(assignedRole);
        companyUser.setCreditsAllocated(creditsToAllocate);
        companyUser.setCreditsUsed(0);
        companyUser.setCompany(company);
        companyUser.setUser(savedUser);
        companyUserRepository.save(companyUser);

        // Record credit allocation in ledger and update company usedCredits
        if (creditsToAllocate > 0) {
            Credit credit = new Credit();
            credit.setAmount(creditsToAllocate);
            credit.setType("allocate");
            credit.setReference("Invite: " + request.email());
            credit.setBalanceAfter(creditsToAllocate);
            credit.setUser(savedUser);
            credit.setCompany(company);
            creditRepository.save(credit);

            company.setUsedCredits(usedCredits + creditsToAllocate);
            companyRepository.save(company);
        }

        // Queue invitation email
        String inviteLink = frontendUrl + "/accept-invitation?token=" + invitationToken;
        queueService.dispatch(JobType.EMAIL_EMPLOYEE_INVITATION, Map.of(
                "to", request.email(),
                "subject", "You're invited to join " + company.getName() + " on TMAG",
                "variables", Map.of(
                        "firstName", firstName,
                        "companyName", company.getName(),
                        "role", assignedRole,
                        "link", inviteLink)));

        return toResponse(savedEmployee);
    }

    /**
     * Maps frontend role names to Role entity names.
     * Frontend sends "HR", but the Roles enum stores "Hr".
     */
    private String normalizeRoleName(String role) {
        if (role == null || role.isBlank()) {
            return "Individual";
        }
        return switch (role.trim().toUpperCase()) {
            case "HR" -> "Hr";
            case "ADMINISTRATOR" -> "Administrator";
            case "INDIVIDUAL" -> "Individual";
            case "SUPERADMIN" -> "SuperAdmin";
            case "CUSTOMERSUPPORT" -> "CustomerSupport";
            default -> role.trim();
        };
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
        String role = null;
        if (entity.getUser() != null) {
            role = companyUserRepository.findByUser(entity.getUser())
                    .map(CompanyUser::getRole)
                    .orElse(null);
        }
        return new EmployeeResponse(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getDepartment(),
            role,
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
