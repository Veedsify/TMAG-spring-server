package com.TravelMedicineAdvisory.Server.core.seeder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.utils.RandomNumberGenerator;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanCode;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.country.Country;
import com.TravelMedicineAdvisory.Server.domain.country.CountryRepository;
import com.TravelMedicineAdvisory.Server.domain.countryhealthalert.CountryHealthAlert;
import com.TravelMedicineAdvisory.Server.domain.countryhealthalert.CountryHealthAlertRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.faqitem.FaqItem;
import com.TravelMedicineAdvisory.Server.domain.faqitem.FaqItemRepository;
import com.TravelMedicineAdvisory.Server.domain.invoice.Invoice;
import com.TravelMedicineAdvisory.Server.domain.invoice.InvoiceRepository;
import com.TravelMedicineAdvisory.Server.domain.permission.Permission;
import com.TravelMedicineAdvisory.Server.domain.permission.PermissionRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermission;
import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermissionRepository;
import com.TravelMedicineAdvisory.Server.domain.systemsetting.SystemSetting;
import com.TravelMedicineAdvisory.Server.domain.systemsetting.SystemSettingRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSetting;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingRepository;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplicationStatus;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Value("${app.features.seeder.enabled:true}")
    private boolean seederEnabled;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final CompanyRepository companyRepository;
    private final CreditPlanRepository userCreditPlanRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyUserRepository companyUserRepository;
    private final CountryRepository countryRepository;
    private final CountryHealthAlertRepository countryHealthAlertRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final FaqItemRepository faqItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;
    private final RandomNumberGenerator randomNumberGenerator;
    private final UserSettingRepository userSettingRepository;

    public DataSeeder(UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            CompanyRepository companyRepository,
            CreditPlanRepository userCreditPlanRepository,
            EmployeeRepository employeeRepository,
            CompanyUserRepository companyUserRepository,
            CountryRepository countryRepository,
            CountryHealthAlertRepository countryHealthAlertRepository,
            SystemSettingRepository systemSettingRepository,
            FaqItemRepository faqItemRepository,
            InvoiceRepository invoiceRepository,
            PasswordEncoder passwordEncoder,
            RandomNumberGenerator randomNumberGenerator,
            UserSettingRepository userSettingRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.companyRepository = companyRepository;
        this.userCreditPlanRepository = userCreditPlanRepository;
        this.employeeRepository = employeeRepository;
        this.companyUserRepository = companyUserRepository;
        this.countryRepository = countryRepository;
        this.countryHealthAlertRepository = countryHealthAlertRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.faqItemRepository = faqItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.passwordEncoder = passwordEncoder;
        this.randomNumberGenerator = randomNumberGenerator;
        this.userSettingRepository = userSettingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!seederEnabled) {
            // logger.info("Database seeder is disabled. Skipping.");
            return;
        }

        // logger.info("Running Database Seeder...");

        this.seedRoles();
        seedPermissions();
        seedRolePermissions();
        seedCompany();
        seedAdminUser();
        seedCountries();
        seedCountryHealthAlerts();
        seedSystemSettings();
        seedFaqItems();
        seedInvoices();
        seedDoctorUser();

        logger.info("Database Seeding Completed.");
    }

    // ======================== ROLES ========================

    @Transactional
    protected void seedRoles() {
        // logger.info("Seeding roles...");

        List<String> roleNames = List.of("SuperAdmin", "Administrator", "HR", "CustomerSupport", "Individual",
                "Doctor", "Affiliate");

        Set<String> existingRoleNames = new HashSet<>();
        roleRepository.findAll().forEach(role -> existingRoleNames.add(role.getName()));

        List<Role> roles = new ArrayList<>();
        for (String name : roleNames) {
            if (existingRoleNames.contains(name)) {
                continue;
            }
            Role role = new Role();
            role.setName(name);
            roles.add(role);
        }
        roleRepository.saveAll(roles);
        // logger.info("Seeded {} roles.", roles.size());
    }

    // ======================== PERMISSIONS ========================

    private static final String[] RESOURCE_TYPES = {
            "user", "authorization", "media", "profile", "abuse_flag",
            "ai_request_log", "api_key", "blog_post", "company", "company_user",
            "country", "country_accommodation", "country_health_alert",
            "credit", "data_export", "doctor", "ebook", "employee", "faq_item",
            "family",
            "health_profile", "invoice", "notification", "plan_generation_context",
            "plan_usage_ledger", "pricing_plan", "report", "system_log", "system_setting",
            "travel_plan", "travel_request", "user_onboarding", "affiliate"
    };

    private static final String[] ACTIONS = { "create", "read", "update", "delete", "list" };

    @Transactional
    protected void seedPermissions() {
        // logger.info("Seeding permissions...");

        Set<String> existingPermissionNames = new HashSet<>();
        permissionRepository.findAll().forEach(p -> existingPermissionNames.add(p.getName()));

        List<Permission> permissions = new ArrayList<>();
        for (String resource : RESOURCE_TYPES) {
            for (String action : ACTIONS) {
                String permissionName = resource + ":" + action;
                if (existingPermissionNames.contains(permissionName)) {
                    continue;
                }

                Permission p = new Permission();
                p.setName(permissionName);
                p.setDescription("Can " + action + " " + resource.replace('_', ' '));
                p.setResourceType(resource);
                p.setAction(action);
                permissions.add(p);
            }
        }
        permissionRepository.saveAll(permissions);
        // logger.info("Seeded {} permissions.", permissions.size());
    }

    // ======================== ROLE-PERMISSIONS ========================

    @Transactional
    protected void seedRolePermissions() {
        // logger.info("Seeding role-permissions...");

        Map<String, Role> roleMap = new HashMap<>();
        roleRepository.findAll().forEach(r -> roleMap.put(r.getName(), r));

        Map<String, Permission> permMap = new HashMap<>();
        permissionRepository.findAll().forEach(p -> permMap.put(p.getName(), p));

        List<RolePermission> assignments = new ArrayList<>();
        Set<String> existingAssignments = new HashSet<>();
        rolePermissionRepository.findAll().forEach(rp -> {
            if (rp.getRole() != null && rp.getPermission() != null) {
                existingAssignments.add(rolePermissionKey(rp.getRole(), rp.getPermission()));
            }
        });

        // SuperAdmin: ALL permissions
        Role superAdmin = roleMap.get("SuperAdmin");
        for (Permission p : permMap.values()) {
            assignments.add(createRolePermission(superAdmin, p));
        }

        // Administrator
        Role administrator = roleMap.get("Administrator");
        String[] adminResources = {
                "user", "authorization", "media", "profile", "abuse_flag", "ai_request_log",
                "api_key", "blog_post", "company", "company_user", "country", "country_accommodation",
                "country_health_alert", "credit", "data_export", "doctor", "ebook", "employee",
                "faq_item", "health_profile", "invoice", "notification", "plan_generation_context",
                "plan_usage_ledger", "pricing_plan", "report", "system_log", "system_setting",
                "travel_plan", "travel_request", "user_onboarding", "affiliate"
        };

        for (String resource : adminResources) {
            for (String action : ACTIONS) {
                Permission p = permMap.get(resource + ":" + action);
                if (p != null)
                    assignments.add(createRolePermission(administrator, p));
            }
        }

        // HR
        Role hr = roleMap.get("HR");
        addPermissions(assignments, hr, permMap, "company", "read", "update");
        addPermissions(assignments, hr, permMap, "employee", "create", "read", "update", "delete", "list");
        addPermissions(assignments, hr, permMap, "travel_request", "read", "list");
        addPermissions(assignments, hr, permMap, "travel_plan", "read", "list");
        addPermissions(assignments, hr, permMap, "invoice", "read", "list");
        addPermissions(assignments, hr, permMap, "credit", "create", "read", "list");
        addPermissions(assignments, hr, permMap, "company_user", "create", "read", "update", "delete", "list");
        addPermissions(assignments, hr, permMap, "data_export", "read", "list");
        addPermissions(assignments, hr, permMap, "plan_usage_ledger", "read", "list");
        addPermissions(assignments, hr, permMap, "pricing_plan", "read", "list");
        addPermissions(assignments, hr, permMap, "report", "read", "list");
        addPermissions(assignments, hr, permMap, "user_onboarding", "read", "update");
        addPermissions(assignments, hr, permMap, "health_profile", "read");
        addPermissions(assignments, hr, permMap, "notification", "read", "list");

        // CustomerSupport
        Role customerSupport = roleMap.get("CustomerSupport");
        addPermissions(assignments, customerSupport, permMap, "user", "read");
        addPermissions(assignments, customerSupport, permMap, "user", "update");
        addPermissions(assignments, customerSupport, permMap, "company", "read");
        addPermissions(assignments, customerSupport, permMap, "employee", "read");
        addPermissions(assignments, customerSupport, permMap, "travel_plan", "read", "list");
        addPermissions(assignments, customerSupport, permMap, "travel_request", "read", "list");
        addPermissions(assignments, customerSupport, permMap, "faq_item", "read", "update");
        addPermissions(assignments, customerSupport, permMap, "credit", "read", "list");
        addPermissions(assignments, customerSupport, permMap, "invoice", "read", "list");
        addPermissions(assignments, customerSupport, permMap, "notification", "read", "list");
        addPermissions(assignments, customerSupport, permMap, "report", "read");
        addPermissions(assignments, customerSupport, permMap, "user_onboarding", "read");
        addPermissions(assignments, customerSupport, permMap, "health_profile", "read");
        addPermissions(assignments, customerSupport, permMap, "pricing_plan", "read", "list");

        // Individual
        Role individual = roleMap.get("Individual");
        addPermissions(assignments, individual, permMap, "profile", "read", "update");
        addPermissions(assignments, individual, permMap, "health_profile", "create", "read", "update", "delete",
                "list");
        addPermissions(assignments, individual, permMap, "travel_plan", "create", "read", "update", "delete", "list");
        addPermissions(assignments, individual, permMap, "travel_request", "create", "read", "update", "delete",
                "list");
        addPermissions(assignments, individual, permMap, "credit", "create", "read", "list");
        addPermissions(assignments, individual, permMap, "country", "read");
        addPermissions(assignments, individual, permMap, "blog_post", "read");
        addPermissions(assignments, individual, permMap, "ebook", "read", "list");
        addPermissions(assignments, individual, permMap, "faq_item", "read");
        addPermissions(assignments, individual, permMap, "family", "read");
        addPermissions(assignments, individual, permMap, "pricing_plan", "read", "list");
        addPermissions(assignments, individual, permMap, "report", "read");
        addPermissions(assignments, individual, permMap, "notification", "read");
        addPermissions(assignments, individual, permMap, "user_onboarding", "create", "read", "update");

        // Doctor
        Role doctor = roleMap.get("Doctor");
        addPermissions(assignments, doctor, permMap, "profile", "read", "update");
        addPermissions(assignments, doctor, permMap, "doctor", "create", "read", "update");
        addPermissions(assignments, doctor, permMap, "travel_plan", "read", "update", "list");
        addPermissions(assignments, doctor, permMap, "health_profile", "read");
        addPermissions(assignments, doctor, permMap, "country", "read");
        addPermissions(assignments, doctor, permMap, "blog_post", "read");
        addPermissions(assignments, doctor, permMap, "ebook", "read", "list");
        addPermissions(assignments, doctor, permMap, "faq_item", "read");
        addPermissions(assignments, doctor, permMap, "notification", "read", "list");
        addPermissions(assignments, doctor, permMap, "pricing_plan", "read", "list");
        addPermissions(assignments, doctor, permMap, "user_onboarding", "read");

        // Affiliate
        Role affiliate = roleMap.get("Affiliate");
        addPermissions(assignments, affiliate, permMap, "profile", "read", "update");
        addPermissions(assignments, affiliate, permMap, "affiliate", "create", "read", "update", "list");
        addPermissions(assignments, affiliate, permMap, "pricing_plan", "read", "list");

        List<RolePermission> newAssignments = assignments.stream()
                .filter(rp -> rp.getRole() != null && rp.getPermission() != null)
                .filter(rp -> existingAssignments.add(rolePermissionKey(rp.getRole(), rp.getPermission())))
                .toList();

        rolePermissionRepository.saveAll(newAssignments);
        // logger.info("Seeded {} role-permission assignments.", assignments.size());
    }

    private String rolePermissionKey(Role role, Permission permission) {
        return role.getId() + ":" + permission.getId();
    }

    private RolePermission createRolePermission(Role role, Permission permission) {
        RolePermission rp = new RolePermission();
        rp.setRole(role);
        rp.setPermission(permission);
        return rp;
    }

    private void addPermissions(List<RolePermission> assignments, Role role,
            Map<String, Permission> permMap, String resource, String... actions) {
        for (String action : actions) {
            Permission p = permMap.get(resource + ":" + action);
            if (p != null)
                assignments.add(createRolePermission(role, p));
        }
    }

    // ======================== COMPANY ========================

    @Transactional
    protected void seedCompany() {
        if (companyRepository.count() > 0)
            return;

        Company company = new Company();
        company.setName("TechCorp Global");
        company.setIndustry("Technology");
        company.setPlan(CreditPlanCode.STANDARD.name());
        userCreditPlanRepository.findByCode(CreditPlanCode.STANDARD).ifPresent(company::setCreditPlan);
        company.setTotalCredits(0);
        company.setUsedCredits(0);
        company.setEmployeeCount(0);
        company.setCompanyCode("TMA-" + randomNumberGenerator.generateNumber());
        company.setBillingStatus(com.TravelMedicineAdvisory.Server.domain.company.BillingStatus.ACTIVE);
        company.setContactEmail("admin@techcorp.com");
        company.setContactPhone("+1 234 567 8900");
        company.setAddress("123 Business St, San Francisco, CA 94105");
        company.setWebsite("https://techcorp.com");

        companyRepository.save(company);
    }

    // ======================== ADMIN USER ========================

    @Transactional
    protected void seedAdminUser() {
        if (userRepository.count() > 1)
            return;

        Role superAdminRole = roleRepository.findByName("SuperAdmin").orElse(null);
        Role userRole = roleRepository.findByName("Individual").orElse(null);
        Role adminRole = roleRepository.findByName("Administrator").orElse(null);
        Role hrRole = roleRepository.findByName("HR").orElse(null);

        Company company = companyRepository.findAll().stream().findFirst().orElse(null);

        // 1. SuperAdmin — platform-level, no company
        User superAdmin = new User();
        superAdmin.setFirstName("Super");
        superAdmin.setLastName("Admin");
        superAdmin.setEmail("super@tmag.com");
        superAdmin.setUsername("super-admin");
        superAdmin.setPassword(passwordEncoder.encode("admin123"));
        superAdmin.setRole(superAdminRole);
        superAdmin.setType("INDIVIDUAL");
        superAdmin.setVerified(true);
        superAdmin.setOnboardingStage(5);
        superAdmin.setOnboarded(true);
        userRepository.save(superAdmin);

        // 2. Administrator — tied to company
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@tmag.com");
        admin.setUsername("admin-user");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(adminRole);
        admin.setType("COMPANY");
        admin.setVerified(true);
        admin.setCredits(100);
        admin.setOnboardingStage(5);
        admin.setOnboarded(true);
        userRepository.save(admin);

        if (company != null) {
            Employee adminEmployee = new Employee();
            adminEmployee.setName("Admin User");
            adminEmployee.setEmail("admin@tmag.com");
            adminEmployee.setDepartment("Management");
            adminEmployee.setStatus("active");
            adminEmployee.setCreditsUsed(0);
            adminEmployee.setCreditsAllocated(100);
            adminEmployee.setPlansGenerated(0);
            adminEmployee.setCompany(company);
            adminEmployee.setUser(admin);
            employeeRepository.save(adminEmployee);

            CompanyUser adminCompanyUser = new CompanyUser();
            adminCompanyUser.setUser(admin);
            adminCompanyUser.setCompany(company);
            adminCompanyUser.setRole("Administrator");
            companyUserRepository.save(adminCompanyUser);

            company.setEmployeeCount(company.getEmployeeCount() + 1);
            companyRepository.save(company);
        }

        // 3. HR Manager — tied to company
        User hrUser = new User();
        hrUser.setFirstName("HR");
        hrUser.setLastName("Manager");
        hrUser.setEmail("hr@tmag.com");
        hrUser.setUsername("hrmanager");
        hrUser.setRole(hrRole);
        hrUser.setType("COMPANY");
        hrUser.setPassword(passwordEncoder.encode("password"));
        hrUser.setVerified(true);
        hrUser.setCredits(50);
        hrUser.setOnboardingStage(5);
        hrUser.setOnboarded(true);
        userRepository.save(hrUser);

        if (company != null) {
            Employee hrEmployee = new Employee();
            hrEmployee.setName("HR Manager");
            hrEmployee.setEmail("hr@tmag.com");
            hrEmployee.setDepartment("Human Resources");
            hrEmployee.setStatus("active");
            hrEmployee.setCreditsUsed(0);
            hrEmployee.setCreditsAllocated(50);
            hrEmployee.setPlansGenerated(0);
            hrEmployee.setCompany(company);
            hrEmployee.setUser(hrUser);
            employeeRepository.save(hrEmployee);

            CompanyUser hrCompanyUser = new CompanyUser();
            hrCompanyUser.setUser(hrUser);
            hrCompanyUser.setCompany(company);
            hrCompanyUser.setRole("HR");
            companyUserRepository.save(hrCompanyUser);

            company.setEmployeeCount(company.getEmployeeCount() + 1);
            companyRepository.save(company);
        }

        // 4. Default User — tied to company as Individual
        User user = new User();
        user.setFirstName("Dike");
        user.setLastName("Wisdom");
        user.setEmail("dikewisdom787@gmail.com");
        user.setUsername("dikewisdom");
        user.setRole(userRole);
        user.setType("COMPANY");
        user.setPassword(passwordEncoder.encode("password"));
        user.setVerified(true);
        user.setOnboardingStage(5);
        user.setCredits(25);
        user.setOnboarded(true);
        userRepository.save(user);

        if (company != null) {
            Employee userEmployee = new Employee();
            userEmployee.setName("Dike Wisdom");
            userEmployee.setEmail("dikewisdom787@gmail.com");
            userEmployee.setDepartment("Engineering");
            userEmployee.setStatus("active");
            userEmployee.setCreditsUsed(0);
            userEmployee.setCreditsAllocated(25);
            userEmployee.setPlansGenerated(0);
            userEmployee.setCompany(company);
            userEmployee.setUser(user);
            employeeRepository.save(userEmployee);

            CompanyUser userCompanyUser = new CompanyUser();
            userCompanyUser.setUser(user);
            userCompanyUser.setCompany(company);
            userCompanyUser.setRole("Individual");
            companyUserRepository.save(userCompanyUser);

            company.setEmployeeCount(company.getEmployeeCount() + 1);
            companyRepository.save(company);
        }

        // 5. Pure Individual User — not tied to any company
        User individualUser = new User();
        individualUser.setFirstName("Jane");
        individualUser.setLastName("Traveler");
        individualUser.setEmail("jane@example.com");
        individualUser.setUsername("jane-traveler");
        individualUser.setRole(userRole);
        individualUser.setType("INDIVIDUAL");
        individualUser.setPassword(passwordEncoder.encode("password"));
        individualUser.setVerified(true);
        individualUser.setOnboardingStage(5);
        individualUser.setOnboarded(true);
        individualUser.setCredits(1);
        userRepository.save(individualUser);

    }

    @Transactional
    protected void seedDoctorUser() {
        if (userRepository.findByEmail("doctor@tmag.com").isPresent()) {
            return;
        }

        Role doctorRole = roleRepository.findByName("Doctor").orElse(null);
        if (doctorRole == null) {
            logger.warn("Doctor role not found. Skipping doctor user seeding.");
            return;
        }

        User doctorUser = new User();
        doctorUser.setFirstName("John");
        doctorUser.setLastName("Smith");
        doctorUser.setEmail("doctor@tmag.com");
        doctorUser.setUsername("doctor-john");
        doctorUser.setRole(doctorRole);
        doctorUser.setType("INDIVIDUAL");
        doctorUser.setPassword(passwordEncoder.encode("password"));
        doctorUser.setVerified(true);
        doctorUser.setOnboardingStage(5);
        doctorUser.setOnboarded(true);
        doctorUser.setCredits(0);
        userRepository.save(doctorUser);

        UserSetting doctorSetting = new UserSetting();
        doctorSetting.setUser(doctorUser);
        doctorSetting.setMedicalLicenseNumber("TMAG-DOC-001");
        doctorSetting.setDoctorApplicationStatus(DoctorApplicationStatus.APPROVED);
        userSettingRepository.save(doctorSetting);

        logger.info("Seeded doctor user: doctor@tmag.com");
    }

    // ======================== COUNTRIES ========================

    private Country createCountry(String name, String code, String region, String continent,
            String riskLevel, String currency, String language,
            String timezone, String emergencyNumber) {
        Country c = new Country();
        c.setName(name);
        c.setCode(code);
        c.setRegion(region);
        c.setContinent(continent);
        c.setRiskLevel(riskLevel);
        c.setCurrency(currency);
        c.setLanguage(language);
        c.setTimezone(timezone);
        c.setEmergencyNumber(emergencyNumber);
        c.setActive(true);
        return c;
    }

    @Transactional
    protected void seedCountries() {
        if (countryRepository.count() > 0)
            return;
        // logger.info("Seeding countries...");

        List<Country> countries = new ArrayList<>();

        // ==================== AFRICA ====================
        // North Africa
        countries.add(createCountry("Algeria", "DZ", "North Africa", "Africa", "Moderate", "DZD", "Arabic, French",
                "UTC+1", "14"));
        countries.add(
                createCountry("Egypt", "EG", "North Africa", "Africa", "Moderate", "EGP", "Arabic", "UTC+2", "122"));
        countries.add(createCountry("Libya", "LY", "North Africa", "Africa", "High", "LYD", "Arabic", "UTC+2", "1515"));
        countries.add(createCountry("Morocco", "MA", "North Africa", "Africa", "Low", "MAD", "Arabic, French", "UTC+1",
                "15"));
        countries.add(createCountry("Tunisia", "TN", "North Africa", "Africa", "Moderate", "TND", "Arabic, French",
                "UTC+1", "197"));
        countries.add(createCountry("Sudan", "SD", "North Africa", "Africa", "High", "SDG", "Arabic, English", "UTC+2",
                "999"));
        countries.add(
                createCountry("South Sudan", "SS", "East Africa", "Africa", "High", "SSP", "English", "UTC+2", "999"));

        // West Africa
        countries
                .add(createCountry("Nigeria", "NG", "West Africa", "Africa", "High", "NGN", "English", "UTC+1", "112"));
        countries.add(
                createCountry("Ghana", "GH", "West Africa", "Africa", "Moderate", "GHS", "English", "UTC+0", "112"));
        countries.add(
                createCountry("Senegal", "SN", "West Africa", "Africa", "Moderate", "XOF", "French", "UTC+0", "15"));
        countries.add(createCountry("Mali", "ML", "West Africa", "Africa", "High", "XOF", "French", "UTC+0", "15"));
        countries.add(
                createCountry("Burkina Faso", "BF", "West Africa", "Africa", "High", "XOF", "French", "UTC+0", "17"));
        countries.add(createCountry("Niger", "NE", "West Africa", "Africa", "High", "XOF", "French", "UTC+1", "17"));
        countries.add(createCountry("Ivory Coast", "CI", "West Africa", "Africa", "Moderate", "XOF", "French", "UTC+0",
                "185"));
        countries.add(
                createCountry("Guinea", "GN", "West Africa", "Africa", "High", "GNF", "French", "UTC+0", "442020"));
        countries
                .add(createCountry("Togo", "TG", "West Africa", "Africa", "Moderate", "XOF", "French", "UTC+0", "117"));
        countries.add(
                createCountry("Benin", "BJ", "West Africa", "Africa", "Moderate", "XOF", "French", "UTC+1", "117"));
        countries.add(
                createCountry("Sierra Leone", "SL", "West Africa", "Africa", "High", "SLL", "English", "UTC+0", "999"));
        countries
                .add(createCountry("Liberia", "LR", "West Africa", "Africa", "High", "LRD", "English", "UTC+0", "911"));
        countries.add(createCountry("Mauritania", "MR", "West Africa", "Africa", "High", "MRU", "Arabic, French",
                "UTC+0", "17"));
        countries.add(
                createCountry("Gambia", "GM", "West Africa", "Africa", "Moderate", "GMD", "English", "UTC+0", "117"));
        countries.add(createCountry("Guinea-Bissau", "GW", "West Africa", "Africa", "High", "XOF", "Portuguese",
                "UTC+0", "112"));
        countries.add(
                createCountry("Cape Verde", "CV", "West Africa", "Africa", "Low", "CVE", "Portuguese", "UTC-1", "132"));

        // East Africa
        countries.add(createCountry("Kenya", "KE", "East Africa", "Africa", "High", "KES", "English, Swahili", "UTC+3",
                "999"));
        countries.add(createCountry("Tanzania", "TZ", "East Africa", "Africa", "High", "TZS", "Swahili, English",
                "UTC+3", "112"));
        countries.add(
                createCountry("Ethiopia", "ET", "East Africa", "Africa", "High", "ETB", "Amharic", "UTC+3", "911"));
        countries.add(createCountry("Uganda", "UG", "East Africa", "Africa", "High", "UGX", "English, Swahili", "UTC+3",
                "999"));
        countries.add(createCountry("Rwanda", "RW", "East Africa", "Africa", "Moderate", "RWF",
                "English, French, Kinyarwanda", "UTC+2", "112"));
        countries.add(createCountry("Burundi", "BI", "East Africa", "Africa", "High", "BIF", "French, Kirundi", "UTC+2",
                "117"));
        countries.add(createCountry("Somalia", "SO", "East Africa", "Africa", "High", "SOS", "Somali, Arabic", "UTC+3",
                "888"));
        countries.add(createCountry("Eritrea", "ER", "East Africa", "Africa", "High", "ERN",
                "Tigrinya, Arabic, English", "UTC+3", "112"));
        countries.add(createCountry("Djibouti", "DJ", "East Africa", "Africa", "Moderate", "DJF", "French, Arabic",
                "UTC+3", "17"));
        countries.add(createCountry("Madagascar", "MG", "East Africa", "Africa", "High", "MGA", "French, Malagasy",
                "UTC+3", "117"));
        countries.add(createCountry("Comoros", "KM", "East Africa", "Africa", "Moderate", "KMF",
                "French, Arabic, Comorian", "UTC+3", "17"));
        countries.add(createCountry("Mauritius", "MU", "East Africa", "Africa", "Low", "MUR", "English, French",
                "UTC+4", "999"));
        countries.add(createCountry("Seychelles", "SC", "East Africa", "Africa", "Low", "SCR",
                "English, French, Creole", "UTC+4", "999"));

        // Central Africa
        countries.add(createCountry("Democratic Republic of the Congo", "CD", "Central Africa", "Africa", "High", "CDF",
                "French", "UTC+1", "112"));
        countries.add(createCountry("Republic of the Congo", "CG", "Central Africa", "Africa", "High", "XAF", "French",
                "UTC+1", "117"));
        countries.add(createCountry("Cameroon", "CM", "Central Africa", "Africa", "High", "XAF", "French, English",
                "UTC+1", "112"));
        countries.add(createCountry("Central African Republic", "CF", "Central Africa", "Africa", "High", "XAF",
                "French, Sango", "UTC+1", "117"));
        countries.add(createCountry("Chad", "TD", "Central Africa", "Africa", "High", "XAF", "French, Arabic", "UTC+1",
                "17"));
        countries.add(
                createCountry("Gabon", "GA", "Central Africa", "Africa", "Moderate", "XAF", "French", "UTC+1", "1730"));
        countries.add(createCountry("Equatorial Guinea", "GQ", "Central Africa", "Africa", "Moderate", "XAF",
                "Spanish, French, Portuguese", "UTC+1", "112"));
        countries.add(createCountry("São Tomé and Príncipe", "ST", "Central Africa", "Africa", "Moderate", "STN",
                "Portuguese", "UTC+0", "112"));

        // Southern Africa
        countries.add(createCountry("South Africa", "ZA", "Southern Africa", "Africa", "Moderate", "ZAR", "English",
                "UTC+2", "10111"));
        countries.add(createCountry("Mozambique", "MZ", "Southern Africa", "Africa", "High", "MZN", "Portuguese",
                "UTC+2", "112"));
        countries.add(createCountry("Zimbabwe", "ZW", "Southern Africa", "Africa", "High", "ZWL",
                "English, Shona, Ndebele", "UTC+2", "999"));
        countries.add(
                createCountry("Zambia", "ZM", "Southern Africa", "Africa", "High", "ZMW", "English", "UTC+2", "999"));
        countries.add(createCountry("Malawi", "MW", "Southern Africa", "Africa", "High", "MWK", "English, Chichewa",
                "UTC+2", "997"));
        countries.add(createCountry("Botswana", "BW", "Southern Africa", "Africa", "Moderate", "BWP",
                "English, Setswana", "UTC+2", "999"));
        countries.add(createCountry("Namibia", "NA", "Southern Africa", "Africa", "Moderate", "NAD", "English", "UTC+2",
                "10111"));
        countries.add(createCountry("Angola", "AO", "Southern Africa", "Africa", "High", "AOA", "Portuguese", "UTC+1",
                "113"));
        countries.add(createCountry("Eswatini", "SZ", "Southern Africa", "Africa", "Moderate", "SZL", "English, Swazi",
                "UTC+2", "999"));
        countries.add(createCountry("Lesotho", "LS", "Southern Africa", "Africa", "Moderate", "LSL", "English, Sesotho",
                "UTC+2", "112"));

        // ==================== ASIA ====================
        // East Asia
        countries.add(createCountry("Japan", "JP", "East Asia", "Asia", "Low", "JPY", "Japanese", "UTC+9", "110"));
        countries.add(createCountry("China", "CN", "East Asia", "Asia", "Low", "CNY", "Mandarin", "UTC+8", "110"));
        countries.add(createCountry("South Korea", "KR", "East Asia", "Asia", "Low", "KRW", "Korean", "UTC+9", "112"));
        countries.add(
                createCountry("Mongolia", "MN", "East Asia", "Asia", "Moderate", "MNT", "Mongolian", "UTC+8", "102"));
        countries.add(createCountry("Taiwan", "TW", "East Asia", "Asia", "Low", "TWD", "Mandarin", "UTC+8", "110"));

        // Southeast Asia
        countries.add(createCountry("Thailand", "TH", "Southeast Asia", "Asia", "Low", "THB", "Thai", "UTC+7", "191"));
        countries.add(createCountry("Vietnam", "VN", "Southeast Asia", "Asia", "Moderate", "VND", "Vietnamese", "UTC+7",
                "113"));
        countries.add(createCountry("Indonesia", "ID", "Southeast Asia", "Asia", "Moderate", "IDR", "Indonesian",
                "UTC+7", "112"));
        countries.add(createCountry("Philippines", "PH", "Southeast Asia", "Asia", "Moderate", "PHP",
                "Filipino, English", "UTC+8", "911"));
        countries.add(createCountry("Malaysia", "MY", "Southeast Asia", "Asia", "Low", "MYR", "Malay, English", "UTC+8",
                "999"));
        countries.add(createCountry("Singapore", "SG", "Southeast Asia", "Asia", "Low", "SGD",
                "English, Mandarin, Malay, Tamil", "UTC+8", "999"));
        countries.add(
                createCountry("Cambodia", "KH", "Southeast Asia", "Asia", "Moderate", "KHR", "Khmer", "UTC+7", "117"));
        countries.add(
                createCountry("Myanmar", "MM", "Southeast Asia", "Asia", "High", "MMK", "Burmese", "UTC+6:30", "199"));
        countries.add(createCountry("Laos", "LA", "Southeast Asia", "Asia", "Moderate", "LAK", "Lao", "UTC+7", "191"));
        countries.add(createCountry("Brunei", "BN", "Southeast Asia", "Asia", "Low", "BND", "Malay, English", "UTC+8",
                "991"));
        countries.add(createCountry("Timor-Leste", "TL", "Southeast Asia", "Asia", "Moderate", "USD",
                "Portuguese, Tetum", "UTC+9", "112"));

        // South Asia
        countries.add(createCountry("India", "IN", "South Asia", "Asia", "Moderate", "INR", "Hindi, English",
                "UTC+5:30", "112"));
        countries.add(
                createCountry("Nepal", "NP", "South Asia", "Asia", "Moderate", "NPR", "Nepali", "UTC+5:45", "100"));
        countries.add(createCountry("Sri Lanka", "LK", "South Asia", "Asia", "Moderate", "LKR", "Sinhala, Tamil",
                "UTC+5:30", "119"));
        countries
                .add(createCountry("Bangladesh", "BD", "South Asia", "Asia", "High", "BDT", "Bengali", "UTC+6", "999"));
        countries.add(
                createCountry("Pakistan", "PK", "South Asia", "Asia", "High", "PKR", "Urdu, English", "UTC+5", "15"));
        countries.add(createCountry("Afghanistan", "AF", "South Asia", "Asia", "High", "AFN", "Pashto, Dari",
                "UTC+4:30", "119"));
        countries.add(createCountry("Bhutan", "BT", "South Asia", "Asia", "Low", "BTN", "Dzongkha", "UTC+6", "112"));
        countries.add(createCountry("Maldives", "MV", "South Asia", "Asia", "Low", "MVR", "Dhivehi", "UTC+5", "119"));

        // Central Asia
        countries.add(createCountry("Kazakhstan", "KZ", "Central Asia", "Asia", "Low", "KZT", "Kazakh, Russian",
                "UTC+6", "112"));
        countries.add(
                createCountry("Uzbekistan", "UZ", "Central Asia", "Asia", "Moderate", "UZS", "Uzbek", "UTC+5", "101"));
        countries.add(createCountry("Kyrgyzstan", "KG", "Central Asia", "Asia", "Moderate", "KGS", "Kyrgyz, Russian",
                "UTC+6", "112"));
        countries.add(
                createCountry("Tajikistan", "TJ", "Central Asia", "Asia", "Moderate", "TJS", "Tajik", "UTC+5", "112"));
        countries.add(createCountry("Turkmenistan", "TM", "Central Asia", "Asia", "Moderate", "TMT", "Turkmen", "UTC+5",
                "03"));

        // Middle East
        countries.add(createCountry("United Arab Emirates", "AE", "Middle East", "Asia", "Low", "AED",
                "Arabic, English", "UTC+4", "999"));
        countries.add(
                createCountry("Saudi Arabia", "SA", "Middle East", "Asia", "Low", "SAR", "Arabic", "UTC+3", "911"));
        countries.add(createCountry("Israel", "IL", "Middle East", "Asia", "Moderate", "ILS", "Hebrew, Arabic", "UTC+2",
                "100"));
        countries.add(createCountry("Jordan", "JO", "Middle East", "Asia", "Low", "JOD", "Arabic", "UTC+2", "911"));
        countries.add(createCountry("Qatar", "QA", "Middle East", "Asia", "Low", "QAR", "Arabic", "UTC+3", "999"));
        countries.add(createCountry("Oman", "OM", "Middle East", "Asia", "Low", "OMR", "Arabic", "UTC+4", "9999"));
        countries.add(createCountry("Kuwait", "KW", "Middle East", "Asia", "Low", "KWD", "Arabic", "UTC+3", "112"));
        countries.add(createCountry("Bahrain", "BH", "Middle East", "Asia", "Low", "BHD", "Arabic", "UTC+3", "999"));
        countries.add(createCountry("Lebanon", "LB", "Middle East", "Asia", "Moderate", "LBP", "Arabic, French",
                "UTC+2", "112"));
        countries.add(
                createCountry("Iraq", "IQ", "Middle East", "Asia", "High", "IQD", "Arabic, Kurdish", "UTC+3", "112"));
        countries.add(
                createCountry("Iran", "IR", "Middle East", "Asia", "Moderate", "IRR", "Persian", "UTC+3:30", "115"));
        countries.add(createCountry("Yemen", "YE", "Middle East", "Asia", "High", "YER", "Arabic", "UTC+3", "199"));
        countries.add(createCountry("Syria", "SY", "Middle East", "Asia", "High", "SYP", "Arabic", "UTC+2", "112"));
        countries.add(createCountry("Palestine", "PS", "Middle East", "Asia", "High", "ILS", "Arabic", "UTC+2", "101"));

        // Caucasus
        countries.add(createCountry("Georgia", "GE", "Caucasus", "Asia", "Low", "GEL", "Georgian", "UTC+4", "112"));
        countries.add(createCountry("Armenia", "AM", "Caucasus", "Asia", "Low", "AMD", "Armenian", "UTC+4", "112"));
        countries.add(
                createCountry("Azerbaijan", "AZ", "Caucasus", "Asia", "Low", "AZN", "Azerbaijani", "UTC+4", "112"));

        // ==================== EUROPE ====================
        // Western Europe
        countries.add(createCountry("United Kingdom", "GB", "Northern Europe", "Europe", "Low", "GBP", "English",
                "UTC+0", "999"));
        countries
                .add(createCountry("France", "FR", "Western Europe", "Europe", "Low", "EUR", "French", "UTC+1", "112"));
        countries.add(
                createCountry("Germany", "DE", "Western Europe", "Europe", "Low", "EUR", "German", "UTC+1", "112"));
        countries.add(
                createCountry("Netherlands", "NL", "Western Europe", "Europe", "Low", "EUR", "Dutch", "UTC+1", "112"));
        countries.add(createCountry("Belgium", "BE", "Western Europe", "Europe", "Low", "EUR", "Dutch, French, German",
                "UTC+1", "112"));
        countries.add(createCountry("Switzerland", "CH", "Western Europe", "Europe", "Low", "CHF",
                "German, French, Italian", "UTC+1", "112"));
        countries.add(
                createCountry("Austria", "AT", "Western Europe", "Europe", "Low", "EUR", "German", "UTC+1", "112"));
        countries.add(createCountry("Luxembourg", "LU", "Western Europe", "Europe", "Low", "EUR",
                "French, German, Luxembourgish", "UTC+1", "112"));
        countries
                .add(createCountry("Monaco", "MC", "Western Europe", "Europe", "Low", "EUR", "French", "UTC+1", "112"));
        countries.add(createCountry("Liechtenstein", "LI", "Western Europe", "Europe", "Low", "CHF", "German", "UTC+1",
                "112"));

        // Northern Europe
        countries.add(
                createCountry("Sweden", "SE", "Northern Europe", "Europe", "Low", "SEK", "Swedish", "UTC+1", "112"));
        countries.add(
                createCountry("Norway", "NO", "Northern Europe", "Europe", "Low", "NOK", "Norwegian", "UTC+1", "112"));
        countries.add(
                createCountry("Denmark", "DK", "Northern Europe", "Europe", "Low", "DKK", "Danish", "UTC+1", "112"));
        countries.add(createCountry("Finland", "FI", "Northern Europe", "Europe", "Low", "EUR", "Finnish, Swedish",
                "UTC+2", "112"));
        countries.add(
                createCountry("Iceland", "IS", "Northern Europe", "Europe", "Low", "ISK", "Icelandic", "UTC+0", "112"));
        countries.add(createCountry("Ireland", "IE", "Northern Europe", "Europe", "Low", "EUR", "English, Irish",
                "UTC+0", "112"));
        countries.add(
                createCountry("Estonia", "EE", "Northern Europe", "Europe", "Low", "EUR", "Estonian", "UTC+2", "112"));
        countries.add(
                createCountry("Latvia", "LV", "Northern Europe", "Europe", "Low", "EUR", "Latvian", "UTC+2", "112"));
        countries.add(createCountry("Lithuania", "LT", "Northern Europe", "Europe", "Low", "EUR", "Lithuanian", "UTC+2",
                "112"));

        // Southern Europe
        countries.add(
                createCountry("Italy", "IT", "Southern Europe", "Europe", "Low", "EUR", "Italian", "UTC+1", "112"));
        countries.add(
                createCountry("Spain", "ES", "Southern Europe", "Europe", "Low", "EUR", "Spanish", "UTC+1", "112"));
        countries.add(createCountry("Portugal", "PT", "Southern Europe", "Europe", "Low", "EUR", "Portuguese", "UTC+0",
                "112"));
        countries
                .add(createCountry("Greece", "GR", "Southern Europe", "Europe", "Low", "EUR", "Greek", "UTC+2", "112"));
        countries.add(
                createCountry("Croatia", "HR", "Southern Europe", "Europe", "Low", "EUR", "Croatian", "UTC+1", "112"));
        countries.add(createCountry("Slovenia", "SI", "Southern Europe", "Europe", "Low", "EUR", "Slovenian", "UTC+1",
                "112"));
        countries.add(
                createCountry("Serbia", "RS", "Southern Europe", "Europe", "Low", "RSD", "Serbian", "UTC+1", "112"));
        countries.add(createCountry("Bosnia and Herzegovina", "BA", "Southern Europe", "Europe", "Low", "BAM",
                "Bosnian, Croatian, Serbian", "UTC+1", "112"));
        countries.add(createCountry("Montenegro", "ME", "Southern Europe", "Europe", "Low", "EUR", "Montenegrin",
                "UTC+1", "112"));
        countries.add(createCountry("North Macedonia", "MK", "Southern Europe", "Europe", "Low", "MKD", "Macedonian",
                "UTC+1", "112"));
        countries.add(
                createCountry("Albania", "AL", "Southern Europe", "Europe", "Low", "ALL", "Albanian", "UTC+1", "112"));
        countries.add(createCountry("Kosovo", "XK", "Southern Europe", "Europe", "Low", "EUR", "Albanian, Serbian",
                "UTC+1", "112"));
        countries.add(createCountry("Malta", "MT", "Southern Europe", "Europe", "Low", "EUR", "Maltese, English",
                "UTC+1", "112"));
        countries.add(createCountry("Cyprus", "CY", "Southern Europe", "Europe", "Low", "EUR", "Greek, Turkish",
                "UTC+2", "112"));
        countries.add(
                createCountry("Andorra", "AD", "Southern Europe", "Europe", "Low", "EUR", "Catalan", "UTC+1", "112"));

        // Central Europe
        countries
                .add(createCountry("Poland", "PL", "Central Europe", "Europe", "Low", "PLN", "Polish", "UTC+1", "112"));
        countries.add(createCountry("Czech Republic", "CZ", "Central Europe", "Europe", "Low", "CZK", "Czech", "UTC+1",
                "112"));
        countries.add(
                createCountry("Slovakia", "SK", "Central Europe", "Europe", "Low", "EUR", "Slovak", "UTC+1", "112"));
        countries.add(
                createCountry("Hungary", "HU", "Central Europe", "Europe", "Low", "HUF", "Hungarian", "UTC+1", "112"));
        countries.add(
                createCountry("Romania", "RO", "Central Europe", "Europe", "Low", "RON", "Romanian", "UTC+2", "112"));
        countries.add(
                createCountry("Bulgaria", "BG", "Central Europe", "Europe", "Low", "BGN", "Bulgarian", "UTC+2", "112"));

        // Eastern Europe
        countries.add(createCountry("Turkey", "TR", "Western Asia", "Europe", "Low", "TRY", "Turkish", "UTC+3", "112"));
        countries.add(
                createCountry("Ukraine", "UA", "Eastern Europe", "Europe", "High", "UAH", "Ukrainian", "UTC+2", "112"));
        countries.add(createCountry("Russia", "RU", "Eastern Europe", "Europe", "Moderate", "RUB", "Russian", "UTC+3",
                "112"));
        countries.add(createCountry("Belarus", "BY", "Eastern Europe", "Europe", "Moderate", "BYN",
                "Belarusian, Russian", "UTC+3", "112"));
        countries.add(
                createCountry("Moldova", "MD", "Eastern Europe", "Europe", "Low", "MDL", "Romanian", "UTC+2", "112"));

        // ==================== NORTH AMERICA ====================
        countries.add(createCountry("United States", "US", "North America", "North America", "Low", "USD", "English",
                "UTC-5", "911"));
        countries.add(createCountry("Canada", "CA", "North America", "North America", "Low", "CAD", "English, French",
                "UTC-5", "911"));
        countries.add(createCountry("Mexico", "MX", "Central America", "North America", "Moderate", "MXN", "Spanish",
                "UTC-6", "911"));

        // Central America
        countries.add(createCountry("Guatemala", "GT", "Central America", "North America", "Moderate", "GTQ", "Spanish",
                "UTC-6", "110"));
        countries.add(createCountry("Belize", "BZ", "Central America", "North America", "Moderate", "BZD", "English",
                "UTC-6", "911"));
        countries.add(createCountry("Honduras", "HN", "Central America", "North America", "High", "HNL", "Spanish",
                "UTC-6", "911"));
        countries.add(createCountry("El Salvador", "SV", "Central America", "North America", "Moderate", "USD",
                "Spanish", "UTC-6", "911"));
        countries.add(createCountry("Nicaragua", "NI", "Central America", "North America", "Moderate", "NIO", "Spanish",
                "UTC-6", "118"));
        countries.add(createCountry("Costa Rica", "CR", "Central America", "North America", "Low", "CRC", "Spanish",
                "UTC-6", "911"));
        countries.add(createCountry("Panama", "PA", "Central America", "North America", "Moderate", "PAB", "Spanish",
                "UTC-5", "911"));

        // Caribbean
        countries.add(createCountry("Cuba", "CU", "Caribbean", "North America", "Moderate", "CUP", "Spanish", "UTC-5",
                "106"));
        countries.add(createCountry("Jamaica", "JM", "Caribbean", "North America", "Moderate", "JMD", "English",
                "UTC-5", "119"));
        countries.add(createCountry("Dominican Republic", "DO", "Caribbean", "North America", "Moderate", "DOP",
                "Spanish", "UTC-4", "911"));
        countries.add(createCountry("Haiti", "HT", "Caribbean", "North America", "High", "HTG",
                "French, Haitian Creole", "UTC-5", "114"));
        countries.add(createCountry("Trinidad and Tobago", "TT", "Caribbean", "North America", "Moderate", "TTD",
                "English", "UTC-4", "999"));
        countries.add(
                createCountry("Bahamas", "BS", "Caribbean", "North America", "Low", "BSD", "English", "UTC-5", "919"));
        countries.add(
                createCountry("Barbados", "BB", "Caribbean", "North America", "Low", "BBD", "English", "UTC-4", "211"));
        countries.add(createCountry("Saint Lucia", "LC", "Caribbean", "North America", "Low", "XCD", "English", "UTC-4",
                "999"));
        countries.add(
                createCountry("Grenada", "GD", "Caribbean", "North America", "Low", "XCD", "English", "UTC-4", "911"));
        countries.add(createCountry("Antigua and Barbuda", "AG", "Caribbean", "North America", "Low", "XCD", "English",
                "UTC-4", "999"));

        // ==================== SOUTH AMERICA ====================
        countries.add(createCountry("Brazil", "BR", "South America", "South America", "Moderate", "BRL", "Portuguese",
                "UTC-3", "190"));
        countries.add(createCountry("Colombia", "CO", "South America", "South America", "Moderate", "COP", "Spanish",
                "UTC-5", "123"));
        countries.add(createCountry("Argentina", "AR", "South America", "South America", "Low", "ARS", "Spanish",
                "UTC-3", "911"));
        countries.add(createCountry("Peru", "PE", "South America", "South America", "Moderate", "PEN", "Spanish",
                "UTC-5", "105"));
        countries.add(createCountry("Chile", "CL", "South America", "South America", "Low", "CLP", "Spanish", "UTC-4",
                "131"));
        countries.add(createCountry("Ecuador", "EC", "South America", "South America", "Moderate", "USD", "Spanish",
                "UTC-5", "911"));
        countries.add(createCountry("Bolivia", "BO", "South America", "South America", "Moderate", "BOB", "Spanish",
                "UTC-4", "110"));
        countries.add(createCountry("Venezuela", "VE", "South America", "South America", "High", "VES", "Spanish",
                "UTC-4", "171"));
        countries.add(createCountry("Uruguay", "UY", "South America", "South America", "Low", "UYU", "Spanish", "UTC-3",
                "911"));
        countries.add(createCountry("Paraguay", "PY", "South America", "South America", "Moderate", "PYG",
                "Spanish, Guarani", "UTC-4", "911"));
        countries.add(createCountry("Guyana", "GY", "South America", "South America", "Moderate", "GYD", "English",
                "UTC-4", "911"));
        countries.add(createCountry("Suriname", "SR", "South America", "South America", "Moderate", "SRD", "Dutch",
                "UTC-3", "115"));

        // ==================== OCEANIA ====================
        countries.add(createCountry("Australia", "AU", "Oceania", "Oceania", "Low", "AUD", "English", "UTC+10", "000"));
        countries.add(createCountry("New Zealand", "NZ", "Oceania", "Oceania", "Low", "NZD", "English, Maori", "UTC+12",
                "111"));
        countries.add(createCountry("Fiji", "FJ", "Oceania", "Oceania", "Moderate", "FJD", "English, Fijian", "UTC+12",
                "911"));
        countries.add(createCountry("Papua New Guinea", "PG", "Oceania", "Oceania", "High", "PGK", "English, Tok Pisin",
                "UTC+10", "000"));
        countries.add(
                createCountry("Samoa", "WS", "Oceania", "Oceania", "Low", "WST", "Samoan, English", "UTC+13", "999"));
        countries.add(
                createCountry("Tonga", "TO", "Oceania", "Oceania", "Low", "TOP", "Tongan, English", "UTC+13", "911"));
        countries.add(createCountry("Vanuatu", "VU", "Oceania", "Oceania", "Moderate", "VUV",
                "English, French, Bislama", "UTC+11", "112"));
        countries.add(createCountry("Solomon Islands", "SB", "Oceania", "Oceania", "Moderate", "SBD", "English",
                "UTC+11", "999"));

        countryRepository.saveAll(countries);
        // logger.info("Seeded {} countries.", countries.size());
    }

    // ======================== COUNTRY HEALTH ALERTS ========================

    @Transactional
    protected void seedCountryHealthAlerts() {
        if (countryHealthAlertRepository.count() > 0)
            return;
        // logger.info("Seeding country health alerts...");

        String[] codes = { "NG", "KE", "CO", "IN", "BR", "TH", "CD", "ET", "PH", "MX", "PE", "BD", "PK", "TZ", "UG",
                "MZ", "MG", "KH", "MM", "PG" };
        Map<String, Country> countryMap = new HashMap<>();
        for (String code : codes) {
            countryRepository.findByCode(code).ifPresent(c -> countryMap.put(code, c));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixMonthsLater = now.plusMonths(6);
        LocalDateTime oneYearLater = now.plusYears(1);

        List<CountryHealthAlert> alerts = new ArrayList<>();

        // Nigeria
        if (countryMap.containsKey("NG")) {
            alerts.add(createAlert("Malaria - Endemic Risk",
                    "Malaria is endemic throughout Nigeria. Prophylaxis strongly recommended for all travelers. Use insect repellent and sleep under treated bed nets.",
                    "high", "disease", "WHO", "https://www.who.int", now, oneYearLater, countryMap.get("NG")));
            alerts.add(createAlert("Yellow Fever - Vaccination Required",
                    "Yellow fever vaccination certificate is required for entry. Ensure vaccination at least 10 days before travel.",
                    "high", "vaccination", "CDC", "https://www.cdc.gov", now, oneYearLater, countryMap.get("NG")));
        }

        // Kenya
        if (countryMap.containsKey("KE")) {
            alerts.add(createAlert("Malaria - High Risk in Coastal & Western Regions",
                    "Malaria transmission occurs in areas below 2,500m including Nairobi suburbs. Antimalarial prophylaxis recommended.",
                    "high", "disease", "WHO", "https://www.who.int", now, oneYearLater, countryMap.get("KE")));
            alerts.add(createAlert("Yellow Fever - Required from Endemic Areas",
                    "Yellow fever vaccination required for travelers arriving from countries with risk of yellow fever transmission.",
                    "moderate", "vaccination", "CDC", "https://www.cdc.gov", now, oneYearLater, countryMap.get("KE")));
        }

        // Colombia
        if (countryMap.containsKey("CO")) {
            alerts.add(createAlert("Dengue Fever - Active Transmission",
                    "Dengue is present in areas below 2,200m. Use mosquito protection measures. No prophylaxis available\u2014prevention is key.",
                    "moderate", "disease", "PAHO", "https://www.paho.org", now, sixMonthsLater, countryMap.get("CO")));
            alerts.add(createAlert("Yellow Fever - Recommended for Rural Areas",
                    "Yellow fever vaccination recommended for travelers visiting areas at altitudes below 2,300m. Required for some national parks.",
                    "moderate", "vaccination", "CDC", "https://www.cdc.gov", now, oneYearLater, countryMap.get("CO")));
        }

        // India
        if (countryMap.containsKey("IN")) {
            alerts.add(createAlert("Typhoid - High Risk",
                    "Typhoid fever risk throughout India. Vaccination recommended for all travelers. Avoid untreated water and street food in high-risk areas.",
                    "moderate", "disease", "WHO", "https://www.who.int", now, oneYearLater, countryMap.get("IN")));
            alerts.add(createAlert("Hepatitis A - Recommended",
                    "Hepatitis A vaccination recommended for all travelers. Virus is prevalent due to varying sanitation conditions.",
                    "moderate", "vaccination", "CDC", "https://www.cdc.gov", now, oneYearLater, countryMap.get("IN")));
            alerts.add(createAlert("Malaria - Risk in Rural Areas",
                    "Malaria present throughout the year in many parts of India, particularly rural areas. Prophylaxis recommended for travelers to endemic zones.",
                    "high", "disease", "WHO", "https://www.who.int", now, oneYearLater, countryMap.get("IN")));
        }

        // Brazil
        if (countryMap.containsKey("BR")) {
            alerts.add(createAlert("Yellow Fever - Recommended for Most Regions",
                    "Yellow fever vaccination recommended for travelers to most states. Required for visits to Amazon basin and surrounding areas.",
                    "moderate", "vaccination", "CDC", "https://www.cdc.gov", now, oneYearLater, countryMap.get("BR")));
            alerts.add(createAlert("Dengue & Zika - Ongoing Transmission",
                    "Both dengue and Zika virus transmission occur in Brazil. Pregnant women should consult their healthcare provider before travel.",
                    "moderate", "disease", "WHO", "https://www.who.int", now, sixMonthsLater, countryMap.get("BR")));
        }

        // Thailand
        if (countryMap.containsKey("TH")) {
            alerts.add(createAlert("Dengue Fever - Seasonal Risk",
                    "Dengue risk increases during rainy season (May\u2013November). Use insect repellent and wear protective clothing.",
                    "moderate", "disease", "WHO", "https://www.who.int", now, sixMonthsLater, countryMap.get("TH")));
            alerts.add(createAlert("Hepatitis A & Typhoid - Recommended",
                    "Vaccination for Hepatitis A and Typhoid recommended for most travelers to Thailand, especially those eating outside major hotel restaurants.",
                    "low", "vaccination", "CDC", "https://www.cdc.gov", now, oneYearLater, countryMap.get("TH")));
        }

        // DR Congo
        if (countryMap.containsKey("CD")) {
            alerts.add(createAlert("Ebola - Intermittent Outbreaks",
                    "Periodic Ebola outbreaks occur in eastern DRC. Monitor WHO situation reports and avoid contact with wildlife.",
                    "critical", "outbreak", "WHO", "https://www.who.int", now, sixMonthsLater, countryMap.get("CD")));
            alerts.add(createAlert("Malaria - Very High Risk",
                    "Malaria is endemic and highly prevalent throughout DRC. Antimalarial prophylaxis is essential for all travelers.",
                    "high", "disease", "CDC", "https://www.cdc.gov", now, oneYearLater, countryMap.get("CD")));
        }

        // Ethiopia
        if (countryMap.containsKey("ET")) {
            alerts.add(createAlert("Cholera - Active Outbreaks",
                    "Cholera outbreaks reported in several regions. Drink only bottled or treated water. Oral cholera vaccine may be considered.",
                    "high", "outbreak", "WHO", "https://www.who.int", now, sixMonthsLater, countryMap.get("ET")));
        }

        // Philippines
        if (countryMap.containsKey("PH")) {
            alerts.add(createAlert("Dengue - Year-Round Transmission",
                    "Dengue fever is endemic throughout the Philippines. Risk is higher during the rainy season (June\u2013November).",
                    "moderate", "disease", "WHO", "https://www.who.int", now, oneYearLater, countryMap.get("PH")));
        }

        // Mexico
        if (countryMap.containsKey("MX")) {
            alerts.add(createAlert("Dengue & Chikungunya - Coastal Regions",
                    "Dengue and chikungunya virus transmitted by mosquitoes in coastal and lowland areas. Use insect protection measures.",
                    "moderate", "disease", "PAHO", "https://www.paho.org", now, sixMonthsLater, countryMap.get("MX")));
        }

        // Peru
        if (countryMap.containsKey("PE")) {
            alerts.add(createAlert("Yellow Fever - Amazon Region",
                    "Yellow fever vaccination required for travelers visiting jungle regions below 2,300m, including the Amazon basin.",
                    "high", "vaccination", "CDC", "https://www.cdc.gov", now, oneYearLater, countryMap.get("PE")));
            alerts.add(createAlert("Altitude Sickness - Cusco & Highlands",
                    "Travelers to Cusco (3,400m) and surrounding highlands are at risk of acute mountain sickness. Gradual acclimatization recommended.",
                    "moderate", "environmental", "CDC", "https://www.cdc.gov", now, oneYearLater,
                    countryMap.get("PE")));
        }

        countryHealthAlertRepository.saveAll(alerts);
        // logger.info("Seeded {} country health alerts.", alerts.size());
    }

    private CountryHealthAlert createAlert(String title, String description, String severity,
            String alertType, String source, String sourceUrl,
            LocalDateTime startsAt, LocalDateTime expiresAt,
            Country country) {
        CountryHealthAlert alert = new CountryHealthAlert();
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setSeverity(severity);
        alert.setAlertType(alertType);
        alert.setSource(source);
        alert.setSourceUrl(sourceUrl);
        alert.setStartsAt(startsAt);
        alert.setExpiresAt(expiresAt);
        alert.setActive(true);
        alert.setCountry(country);
        return alert;
    }

    // ======================== SYSTEM SETTINGS ========================

    @Transactional
    protected void seedSystemSettings() {
        if (systemSettingRepository.count() > 0)
            return;
        // logger.info("Seeding system settings...");

        List<SystemSetting> settings = new ArrayList<>();

        settings.add(
                createSetting("site_name", "TMAG", "string", "general", "Site Name", "The name of the platform", true));
        settings.add(createSetting("site_tagline", "Travel Medicine Advisory Global", "string", "general",
                "Site Tagline", "The tagline displayed on the site", true));
        settings.add(createSetting("support_email", "hello@tmag.health", "string", "general", "Support Email",
                "Main support email address", true));
        settings.add(createSetting("support_response_time", "24 hours", "string", "general", "Support Response Time",
                "Typical support response time", true));
        settings.add(createSetting("credit_price_single", "9", "number", "pricing", "Single Credit Price",
                "Price for a single credit in USD", true));
        settings.add(createSetting("credit_price_5_pack", "39", "number", "pricing", "5-Credit Pack Price",
                "Price for 5-credit pack in USD (13% savings)", true));
        settings.add(createSetting("credit_price_10_pack", "69", "number", "pricing", "10-Credit Pack Price",
                "Price for 10-credit pack in USD (23% savings)", true));
        settings.add(createSetting("corporate_50_credit_rate", "5", "number", "pricing", "Corporate 50-Credit Rate",
                "Per-credit price for 50-credit corporate pack in USD", true));
        settings.add(createSetting("corporate_200_credit_rate", "4", "number", "pricing", "Corporate 200-Credit Rate",
                "Per-credit price for 200-credit corporate pack in USD", true));
        settings.add(createSetting("free_credits_on_signup", "1", "number", "pricing", "Free Credits on Signup",
                "Number of free credits given on new account registration", true));
        settings.add(createSetting("refund_window_days", "30", "number", "pricing", "Refund Window (Days)",
                "Number of days unused credits are refundable", true));
        settings.add(createSetting("data_sources", "WHO,CDC,ECDC", "string", "data", "Data Sources",
                "Health data authorities used for plan generation", true));
        settings.add(createSetting("stat_countries_covered", "190+", "string", "stats", "Countries Covered",
                "Number of countries with health data coverage", true));
        settings.add(createSetting("stat_plans_generated", "50K+", "string", "stats", "Plans Generated",
                "Total plans generated to date", true));
        settings.add(
                createSetting("stat_uptime", "99.9%", "string", "stats", "Uptime", "Platform uptime percentage", true));
        settings.add(createSetting("stat_user_rating", "4.9", "string", "stats", "User Rating",
                "Average user rating out of 5", true));
        settings.add(createSetting("ndpr_compliant", "true", "boolean", "compliance", "NDPR Compliant",
                "Whether the platform follows NDPR-aligned data handling", true));
        settings.add(createSetting("iso_31030_aligned", "true", "boolean", "compliance", "ISO 31030 Aligned",
                "Whether the platform supports ISO 31030 travel risk management", true));

        systemSettingRepository.saveAll(settings);
        // logger.info("Seeded {} system settings.", settings.size());
    }

    private SystemSetting createSetting(String key, String value, String type, String group,
            String label, String description, boolean isPublic) {
        SystemSetting s = new SystemSetting();
        s.setKey(key);
        s.setValue(value);
        s.setType(type);
        s.setGroup(group);
        s.setLabel(label);
        s.setDescription(description);
        s.setPublic(isPublic);
        return s;
    }

    // ======================== FAQ ITEMS ========================

    @Transactional
    protected void seedFaqItems() {
        if (faqItemRepository.count() > 0)
            return;
        // logger.info("Seeding FAQ items...");

        List<FaqItem> faqs = new ArrayList<>();

        faqs.add(createFaq("What is TMAG?",
                "TMAG (Travel Medicine Advisory Global) is an AI-powered platform that generates personalized travel health plans. We analyze your destination, travel dates, and health profile against real-time data from WHO, CDC, and local health authorities to give you actionable medical guidance.",
                "General", 1));
        faqs.add(createFaq("Is this medical advice?",
                "No. TMAG provides informational health guidance, not medical advice. Our plans are based on publicly available data from global health authorities and are designed to help you prepare\u2014but they are not a substitute for professional medical consultation. Always speak with a doctor before making health decisions.",
                "General", 2));
        faqs.add(createFaq("Who is TMAG for?",
                "Anyone traveling internationally. Solo travelers, families, digital nomads, and corporate travel managers all use TMAG to prepare for trips. We also serve companies that need to meet duty-of-care obligations for employees traveling abroad.",
                "General", 3));
        faqs.add(createFaq("What consumes a credit?",
                "Generating one full travel health plan for one trip consumes one credit. A trip can include multiple destinations. Viewing, downloading, or sharing an existing plan does not consume a credit.",
                "Credits & billing", 4));
        faqs.add(createFaq("Is the first plan really free?",
                "Yes. You can sign up and generate your first plan at no cost, with no credit card required. This lets you see exactly what you\u2019ll get before paying for anything.",
                "Credits & billing", 5));
        faqs.add(createFaq("Do credits expire?",
                "No. Credits never expire. Once purchased, they stay in your account until you use them\u2014whether that\u2019s next week or next year.",
                "Credits & billing", 6));
        faqs.add(createFaq("Can I get a refund?",
                "Unused credits are refundable within 30 days of purchase. Once a credit has been used to generate a plan, it cannot be refunded.",
                "Credits & billing", 7));
        faqs.add(createFaq("What’s included in a plan?",
                "Each plan includes: required and recommended vaccinations, medication recommendations, disease risk alerts, water and food safety guidance, emergency contacts and nearby clinics, insurance considerations, packing checklist, and any pre-existing condition adjustments.",
                "Plans & features", 8));
        faqs.add(createFaq("Can plans be edited after generation?",
                "Plans cannot be directly edited, but you can regenerate a plan with updated information. If your itinerary changes or you want to add health details, simply use another credit to generate an updated version.",
                "Plans & features", 9));
        faqs.add(createFaq("How current is the data?",
                "Our system pulls from continuously updated databases. When you generate a plan, it reflects the latest available data from WHO, CDC, ECDC, and local health authorities at that moment\u2014including active outbreak alerts and seasonal risk changes.",
                "Plans & features", 10));
        faqs.add(createFaq("Can I share my plan with my doctor?",
                "Absolutely. Every plan includes a doctor-ready summary specifically formatted for healthcare providers. You can download the PDF and bring it to your appointment or share it digitally.",
                "Plans & features", 11));
        faqs.add(createFaq("Is my health data safe?",
                "Yes. We follow NDPR-aligned data handling practices. Your health data is encrypted in transit and at rest, never sold to third parties, and can be fully deleted from your account settings at any time.",
                "Privacy & security", 12));
        faqs.add(createFaq("Do you train AI on my data?",
                "No. We do not use your personal health data to train our AI models. Your data is used solely to generate your personalized plan.",
                "Privacy & security", 13));
        faqs.add(createFaq("Can I delete my account and data?",
                "Yes. You can delete your account and all associated data at any time from your account settings. Deletion is permanent and irreversible.",
                "Privacy & security", 14));
        faqs.add(createFaq("How does the corporate plan differ?",
                "Corporate accounts include an HR dashboard for managing travelers, bulk plan generation, compliance reporting, duty-of-care documentation, credit allocation across departments, and API access for integration with travel platforms.",
                "Corporate", 15));
        faqs.add(createFaq("Can we integrate TMAG with our travel booking system?",
                "Yes. We offer a REST API that allows you to trigger plan generation from your travel booking platform, HRIS, or internal tools. We also support CSV batch uploads for bulk operations.",
                "Corporate", 16));
        faqs.add(createFaq("How do we manage traveler data?",
                "Corporate accounts include an HR dashboard for managing travelers, bulk plan generation, compliance reporting, duty-of-care documentation, credit allocation across departments, and API access for integration with travel platforms.",
                "Corporate", 17));
        faqs.add(createFaq("What compliance standards does TMAG support?",
                "TMAG supports ISO 31030 travel risk management alignment, provides timestamped plan delivery with read receipts, and generates exportable compliance reports per trip, employee, or destination.",
                "Corporate", 18));

        faqItemRepository.saveAll(faqs);
        // logger.info("Seeded {} FAQ items.", faqs.size());
    }

    private FaqItem createFaq(String question, String answer, String category, int position) {
        FaqItem faq = new FaqItem();
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setCategory(category);
        faq.setPosition(position);
        faq.setActive(true);
        return faq;
    }

    // ======================== INVOICES ========================

    @Transactional
    protected void seedInvoices() {
        if (invoiceRepository.count() > 0)
            return;

        Company company = companyRepository.findAll().stream().findFirst().orElse(null);
        if (company == null)
            return;

        List<Invoice> invoices = new ArrayList<>();

        invoices.add(createInvoice(new BigDecimal("49.99"), "USD", "paid", "TMAG Starter Plan - 10 Credits",
                LocalDateTime.now().minusDays(30), LocalDateTime.now().minusDays(23),
                LocalDateTime.now().minusDays(23), "Credit Card", company, null));
        invoices.add(createInvoice(new BigDecimal("199.99"), "USD", "paid", "TMAG Business Plan - 50 Credits",
                LocalDateTime.now().minusDays(14), LocalDateTime.now().minusDays(7),
                LocalDateTime.now().minusDays(7), "Credit Card", company, null));
        invoices.add(createInvoice(new BigDecimal("499.99"), "USD", "pending", "TMAG Enterprise Plan - 150 Credits",
                LocalDateTime.now().minusDays(3), LocalDateTime.now().plusDays(27),
                null, "Bank Transfer", company, null));

        invoiceRepository.saveAll(invoices);
    }

    private Invoice createInvoice(BigDecimal amount, String currency, String status, String description,
            LocalDateTime issuedAt, LocalDateTime dueDate, LocalDateTime paidAt,
            String paymentMethod, Company company, User user) {
        Invoice invoice = new Invoice();
        invoice.setAmount(amount);
        invoice.setCurrency(currency);
        invoice.setStatus(status);
        invoice.setDescription(description);
        invoice.setIssuedAt(issuedAt);
        invoice.setDueDate(dueDate);
        invoice.setPaidAt(paidAt);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setCompany(company);
        invoice.setUser(user);
        return invoice;
    }
}
