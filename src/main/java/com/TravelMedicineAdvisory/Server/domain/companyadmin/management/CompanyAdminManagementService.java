package com.TravelMedicineAdvisory.Server.domain.companyadmin.management;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.admin.dataexport.DataExportService;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKey;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanEntity;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanService;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.invoice.InvoiceRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class CompanyAdminManagementService {

    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataExportService dataExportService;
    private final CompanyApiKeyRepository companyApiKeyRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final InvoiceRepository invoiceRepository;
    private final PlanService planService;
    private final QueueService queueService;

    public CompanyAdminManagementService(
            CompanyRepository companyRepository,
            CompanyUserRepository companyUserRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            DataExportService dataExportService,
            CompanyApiKeyRepository companyApiKeyRepository,
            TravelPlanRepository travelPlanRepository,
            InvoiceRepository invoiceRepository,
            PlanService planService,
            QueueService queueService) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataExportService = dataExportService;
        this.companyApiKeyRepository = companyApiKeyRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.invoiceRepository = invoiceRepository;
        this.planService = planService;
        this.queueService = queueService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> viewTeamMembers(Long companyId) {
        return companyUserRepository.findAllByCompanyId(companyId).stream()
                .map(this::mapCompanyUser)
                .toList();
    }

    public Map<String, Object> createUser(CompanyAdminUserCreateRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setName(buildDisplayName(request.firstName(), request.lastName(), request.email()));
        user.setEmail(request.email());
        user.setUsername(request.email());
        user.setPassword(passwordEncoder.encode(request.password() != null ? request.password() : "password"));
        user.setType("COMPANY");
        user.setVerified(true);
        user.setOnboarded(true);
        user.setOnboardingStage(5);
        user.setIsActive(true);
        user.setRole(resolveRole(request.role()));
        user = userRepository.save(user);

        int allocatedCredits = request.creditsAllocated() != null ? request.creditsAllocated() : 0;
        user.setCredits(allocatedCredits);
        userRepository.save(user);

        CompanyUser companyUser = new CompanyUser();
        companyUser.setCompany(company);
        companyUser.setUser(user);
        companyUser.setRole(request.role() != null ? request.role() : "Individual");
        companyUser.setCreditsAllocated(allocatedCredits);
        companyUser.setCreditsUsed(0);
        companyUser = companyUserRepository.save(companyUser);

        Employee employee = new Employee();
        employee.setCompany(company);
        employee.setUser(user);
        employee.setName(user.getName());
        employee.setEmail(user.getEmail());
        employee.setDepartment(request.department());
        employee.setStatus("active");
        employee.setCreditsAllocated(allocatedCredits);
        employee.setCreditsUsed(0);
        employee.setPlansGenerated(0);
        employeeRepository.save(employee);

        return mapCompanyUser(companyUser);
    }

    public Map<String, Object> updateCompanyUser(Long companyUserId, CompanyAdminUserUpdateRequest request) {
        CompanyUser companyUser = companyUserRepository.findById(companyUserId)
                .orElseThrow(() -> new NoSuchElementException("Company user not found"));

        User user = companyUser.getUser();
        if (user == null) {
            throw new NoSuchElementException("Linked user not found");
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmail(request.email()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new IllegalArgumentException("Email already in use");
                }
            });
            user.setEmail(request.email());
            user.setUsername(request.email());
        }
        if (request.role() != null) {
            companyUser.setRole(request.role());
            user.setRole(resolveRole(request.role()));
        }

        Integer allocatedCredits = request.creditsAllocated();
        if (allocatedCredits != null) {
            companyUser.setCreditsAllocated(allocatedCredits);
            user.setCredits(allocatedCredits);
        }

        user.setName(buildDisplayName(user.getFirstName(), user.getLastName(), user.getEmail()));
        userRepository.save(user);

        Employee employee = findEmployeeByUserId(user.getId());
        if (employee != null) {
            if (request.department() != null) {
                employee.setDepartment(request.department());
            }
            if (request.employeeStatus() != null) {
                employee.setStatus(request.employeeStatus());
            }
            if (allocatedCredits != null) {
                employee.setCreditsAllocated(allocatedCredits);
            }
            employee.setName(user.getName());
            employee.setEmail(user.getEmail());
            employeeRepository.save(employee);
        }

        return mapCompanyUser(companyUserRepository.save(companyUser));
    }

    public void deleteCompanyUser(Long companyUserId) {
        CompanyUser companyUser = companyUserRepository.findById(companyUserId)
                .orElseThrow(() -> new NoSuchElementException("Company user not found"));
        User user = companyUser.getUser();

        companyUserRepository.delete(companyUser);

        if (user != null) {
            Employee employee = findEmployeeByUserId(user.getId());
            if (employee != null) {
                employeeRepository.delete(employee);
            }
            userRepository.delete(user);
        }
    }

    public Map<String, Object> restrictUserAccess(Long companyUserId, boolean restricted) {
        CompanyUser companyUser = companyUserRepository.findById(companyUserId)
                .orElseThrow(() -> new NoSuchElementException("Company user not found"));
        if (companyUser.getUser() == null) {
            throw new NoSuchElementException("Linked user not found");
        }
        companyUser.getUser().setIsActive(!restricted);
        User saved = userRepository.save(companyUser.getUser());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user_id", saved.getId());
        result.put("restricted", restricted);
        result.put("is_active", saved.getIsActive());
        return result;
    }

    public void removeEmployee(Long employeeId, Long companyId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        if (employee.getCompany() == null || !employee.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Employee does not belong to this company");
        }
        employeeRepository.delete(employee);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportCompanyData(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("company", Map.of(
                "id", company.getId(),
                "name", company.getName(),
                "plan", resolvePlanCode(company)));
        payload.put("users", viewTeamMembers(companyId));
        payload.put("reports", Map.of(
                "usage_total_plans", travelPlanRepository.countByCompanyId(companyId),
                "billing_total_invoices", invoiceRepository.findByCompanyId(companyId).size()));
        payload.put("plans", dataExportService.exportPlans(companyId));
        payload.put("data", Map.of(
                "employees", dataExportService.exportEmployees(companyId),
                "requests", dataExportService.exportRequests(companyId)));
        payload.put("api_tokens", companyApiKeyRepository.findByCompanyId(companyId).stream()
                .map(this::mapApiKey)
                .toList());
        payload.put("usage", dataExportService.exportUsage(companyId));
        payload.put("billing_history", dataExportService.exportBilling(companyId));
        return payload;
    }

    public void deleteCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        CompanyUser firstAdmin = companyUserRepository.findAdminsByCompanyId(companyId).stream()
                .min(Comparator.comparing(CompanyUser::getId))
                .orElseThrow(() -> new IllegalArgumentException("No company admin found for this company"));

        if (firstAdmin.getUser() == null || firstAdmin.getUser().getEmail() == null) {
            throw new IllegalArgumentException("First company admin email is not available");
        }

        queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                "to", firstAdmin.getUser().getEmail(),
                "subject", "TMAG company deletion notice",
                "variables", Map.of(
                        "title", "Company deletion initiated",
                        "message", "Company " + company.getName()
                                + " has been deleted from TMAG by a company administrator.")));

        companyRepository.delete(company);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPlan(Long planId) {
        return Map.of("plan", planService.findById(planId));
    }

    private Map<String, Object> mapCompanyUser(CompanyUser companyUser) {
        User user = companyUser.getUser();
        Employee employee = user != null ? findEmployeeByUserId(user.getId()) : null;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("company_user_id", companyUser.getId());
        result.put("role", companyUser.getRole());
        result.put("company_id", companyUser.getCompany() != null ? companyUser.getCompany().getId() : null);
        result.put("user_id", user != null ? user.getId() : null);
        result.put("name", user != null ? user.getName() : null);
        result.put("email", user != null ? user.getEmail() : null);
        result.put("is_active", user != null ? user.getIsActive() : null);
        result.put("credits_allocated", companyUser.getCreditsAllocated());
        result.put("credits_used", companyUser.getCreditsUsed());
        result.put("department", employee != null ? employee.getDepartment() : null);
        result.put("employee_status", employee != null ? employee.getStatus() : null);
        return result;
    }

    private Role resolveRole(String roleName) {
        String normalized = roleName == null || roleName.isBlank() ? "Individual" : roleName.trim();
        return roleRepository.findByName(normalized)
                .or(() -> roleRepository.findByName("Individual"))
                .orElseThrow(() -> new NoSuchElementException("Role not found"));
    }

    private Employee findEmployeeByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return employeeRepository.findByUserId(userId).orElse(null);
    }

    private String buildDisplayName(String firstName, String lastName, String email) {
        String name = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        return name.isBlank() ? email : name;
    }

    private String resolvePlanCode(Company company) {
        PlanEntity activePlan = company.getActivePlan();
        if (activePlan != null && activePlan.getCode() != null) {
            return activePlan.getCode().name();
        }
        return company.getPlan();
    }

    private Map<String, Object> mapApiKey(CompanyApiKey key) {
        return Map.of(
                "id", key.getId(),
                "name", key.getName(),
                "prefix", key.getKeyPrefix(),
                "status", key.getStatus() != null ? key.getStatus().name() : null,
                "created_at", key.getCreatedAt(),
                "expires_at", key.getExpiresAt());
    }
}
