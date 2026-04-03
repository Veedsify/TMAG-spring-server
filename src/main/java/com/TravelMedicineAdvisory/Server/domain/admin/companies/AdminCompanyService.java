package com.TravelMedicineAdvisory.Server.domain.admin.companies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.notifications.AdminNotificationService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.core.utils.RandomNumberGenerator;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.company.BillingStatus;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.company.Tier;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Roles;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
public class AdminCompanyService {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyUserRepository companyUserRepository;
    private final CreditRepository creditRepository;
    private final RandomNumberGenerator randomNumberGenerator;
    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final QueueService queueService;

    public AdminCompanyService(CompanyRepository companyRepository,
            EmployeeRepository employeeRepository,
            CompanyUserRepository companyUserRepository,
            CreditRepository creditRepository,
            RandomNumberGenerator randomNumberGenerator,
            TravelPlanRepository travelPlanRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            QueueService queueService) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.companyUserRepository = companyUserRepository;
        this.randomNumberGenerator = randomNumberGenerator;
        this.creditRepository = creditRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.queueService = queueService;
    }

    public List<AdminCompanyResponse> findAll() {
        List<Company> companies = companyRepository.findAllActive();
        return companies.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public AdminCompanyResponse create(Map<String, Object> body) {

        String companyCode = String.format("TMA-%s", randomNumberGenerator.generateNumber());

        Company company = new Company();
        if (body.containsKey("name")) {
            company.setName((String) body.get("name"));
        }
        if (body.containsKey("industry")) {
            company.setIndustry((String) body.get("industry"));
        }
        if (body.containsKey("website")) {
            company.setWebsite((String) body.get("website"));
        }
        if (body.containsKey("contactEmail")) {
            company.setContactEmail((String) body.get("contactEmail"));
        }
        if (body.containsKey("contactPhone")) {
            company.setContactPhone((String) body.get("contactPhone"));
        }
        if (body.containsKey("address")) {
            company.setAddress((String) body.get("address"));
        }
        if (body.containsKey("billingCurrency")) {
            try {
                company.setBillingCurrency(
                        BillingCurrency.valueOf(((String) body.get("billingCurrency")).toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        company.setTotalCredits(0);
        company.setUsedCredits(0);
        company.setEmployeeCount(0);
        company.setCompanyCode(companyCode);
        company.setBillingStatus(BillingStatus.ACTIVE);
        company.setTier(Tier.STANDARD);
        company = companyRepository.save(company);

        // Create default HR Admin user for this company
        String adminName = body.containsKey("adminName") ? (String) body.get("adminName") : null;
        String adminEmail = body.containsKey("adminEmail") ? (String) body.get("adminEmail") : null;
        String adminPassword = body.containsKey("adminPassword") ? (String) body.get("adminPassword") : null;

        String role = String.valueOf(Roles.Administrator);
        Optional<Role> adminRole = roleRepository.findByName(role);

        if (adminEmail != null && adminPassword != null) {
            User adminUser = new User();
            adminUser.setName(adminName != null ? adminName : adminEmail);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setType("COMPANY");
            adminUser.setVerified(true);
            if (adminRole != null) {
                adminUser.setRole(adminRole.get());
            }
            adminUser.setOnboarded(true);
            adminUser.setMustChangePassword(true);
            adminUser.setIsActive(true);
            if (company.getBillingCurrency() != null) {
                adminUser.setBillingCurrency(company.getBillingCurrency());
            }
            adminUser = userRepository.save(adminUser);

            Employee adminEmployee = new Employee();
            adminEmployee.setName(adminUser.getName());
            adminEmployee.setEmail(adminUser.getEmail());
            adminEmployee.setDepartment("Administration");
            adminEmployee.setStatus("active");
            adminEmployee.setCreditsUsed(0);
            adminEmployee.setCreditsAllocated(0);
            adminEmployee.setPlansGenerated(0);
            adminEmployee.setCompany(company);
            adminEmployee.setUser(adminUser);
            employeeRepository.save(adminEmployee);

            CompanyUser companyUser = new CompanyUser();
            companyUser.setRole("Administrator");
            companyUser.setCreditsAllocated(0);
            companyUser.setCreditsUsed(0);
            companyUser.setCompany(company);
            companyUser.setUser(adminUser);
            companyUserRepository.save(companyUser);

            sendOnboardingEmail(adminUser, company, adminPassword);
        }

        return mapToResponse(company);
    }

    private void sendOnboardingEmail(User user, Company company, String temporaryPassword) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "there";
        queueService.dispatch(JobType.EMAIL_COMPANY_ADMIN_ONBOARDING, Map.of(
                "to", user.getEmail(),
                "subject", "Welcome to TMAG - Your admin account is ready",
                "variables", Map.of(
                        "firstName", firstName,
                        "companyName", company.getName(),
                        "temporaryPassword", temporaryPassword != null ? temporaryPassword : "")));
    }

    public AdminCompanyResponse findById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return mapToResponse(company);
    }

    public List<AdminCompanyEmployeeResponse> getEmployees(Long id) {
        List<Employee> employees = employeeRepository.findAll();
        List<Employee> companyEmployees = employees.stream()
                .filter(e -> e.getCompany() != null && e.getCompany().getId().equals(id))
                .toList();

        List<AdminCompanyEmployeeResponse> result = new ArrayList<>();
        for (Employee emp : companyEmployees) {
            AdminCompanyEmployeeResponse empResponse = new AdminCompanyEmployeeResponse();
            empResponse.setId(emp.getId());
            empResponse.setName(emp.getName());
            empResponse.setEmail(emp.getEmail());
            empResponse.setDepartment(emp.getDepartment());
            empResponse.setStatus(emp.getStatus());
            empResponse.setCreditsAllocated(emp.getCreditsAllocated() != null ? emp.getCreditsAllocated() : 0);
            empResponse.setCreditsUsed(emp.getCreditsUsed() != null ? emp.getCreditsUsed() : 0);
            empResponse.setPlansGenerated(emp.getPlansGenerated() != null ? emp.getPlansGenerated() : 0);
            if (emp.getUser() != null) {
                empResponse.setUserId(emp.getUser().getId());
                empResponse.setAvatar(emp.getUser().getAvatarUrl());
            }
            result.add(empResponse);
        }

        return result;
    }

    @Transactional
    public AdminCompanyResponse update(Long id, Map<String, Object> updates) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (updates.containsKey("name")) {
            company.setName((String) updates.get("name"));
        }
        if (updates.containsKey("industry")) {
            company.setIndustry((String) updates.get("industry"));
        }
        if (updates.containsKey("website")) {
            company.setWebsite((String) updates.get("website"));
        }
        if (updates.containsKey("address")) {
            company.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("contactEmail")) {
            company.setContactEmail((String) updates.get("contactEmail"));
        }
        if (updates.containsKey("contactPhone")) {
            company.setContactPhone((String) updates.get("contactPhone"));
        }
        if (updates.containsKey("plan")) {
            company.setPlan((String) updates.get("plan"));
        }

        company = companyRepository.save(company);
        return mapToResponse(company);
    }

    @Transactional
    public void freeze(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setBillingStatus(BillingStatus.FROZEN);
        companyRepository.save(company);
    }

    @Transactional
    public void unfreeze(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setBillingStatus(BillingStatus.ACTIVE);
        companyRepository.save(company);
    }

    @Transactional
    public void addCredits(Long id, Integer amount) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Integer currentCredits = company.getTotalCredits() != null ? company.getTotalCredits() : 0;

        Credit credit = new Credit();
        credit.setCompany(company);
        credit.setAmount(amount);
        credit.setType("admin_add");
        credit.setBalanceAfter(currentCredits + amount);
        credit.setReference("Admin credit addition for company " + id);

        creditRepository.save(credit);

        company.setTotalCredits(currentCredits + amount);
        companyRepository.save(company);
    }

    @Transactional
    public void upgradeTier(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setTier(Tier.ENTERPRISE);
        companyRepository.save(company);
    }

    @Transactional
    public void delete(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        companyRepository.delete(company);
    }

    private AdminCompanyResponse mapToResponse(Company company) {
        Integer totalCredits = company.getTotalCredits() != null ? company.getTotalCredits() : 0;
        Integer usedCredits = company.getUsedCredits() != null ? company.getUsedCredits() : 0;

        List<CompanyUser> companyUsers = companyUserRepository.findAll();
        List<CompanyUser> companyUserList = companyUsers.stream()
                .filter(cu -> cu.getCompany() != null && cu.getCompany().getId().equals(company.getId()))
                .toList();

        List<String> hrAdmins = new ArrayList<>();
        for (CompanyUser cu : companyUserList) {
            if (cu.getUser() != null && "HR_ADMIN".equals(cu.getRole())) {
                hrAdmins.add(cu.getUser().getName() != null ? cu.getUser().getName() : cu.getUser().getEmail());
            }
        }

        List<Employee> allEmployees = employeeRepository.findAll();
        long activeEmployees = allEmployees.stream()
                .filter(e -> e.getCompany() != null && e.getCompany().getId().equals(company.getId()))
                .filter(e -> "active".equals(e.getStatus()))
                .count();

        long plansGenerated = travelPlanRepository.countByCompanyId(company.getId());

        String billingStatus = "active";
        if (company.getBillingStatus() != null) {
            billingStatus = company.getBillingStatus().name().toLowerCase();
        }

        String tier = "standard";
        if (company.getTier() != null) {
            tier = company.getTier().name().toLowerCase();
        }

        String billingCurrency = company.getBillingCurrency() != null
                ? company.getBillingCurrency().name()
                : BillingCurrency.NGN.name();

        return new AdminCompanyResponse(
                company.getId(),
                company.getName(),
                company.getIndustry(),
                company.getWebsite(),
                company.getTotalCredits() != null ? company.getTotalCredits() : 0,
                totalCredits - usedCredits,
                (int) plansGenerated,
                (int) activeEmployees,
                billingStatus,
                company.getContractRenewal(),
                tier,
                hrAdmins,
                company.getContactEmail(),
                company.getContactPhone(),
                company.getAddress(),
                billingCurrency,
                company.getCreatedAt());
    }
}
