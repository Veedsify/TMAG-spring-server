package com.TravelMedicineAdvisory.Server.domain.admin.companies;

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
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminCompanyService {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyUserRepository companyUserRepository;
    private final CreditRepository creditRepository;
    private final TravelPlanRepository travelPlanRepository;

    public AdminCompanyService(CompanyRepository companyRepository, 
                               EmployeeRepository employeeRepository,
                               CompanyUserRepository companyUserRepository,
                               CreditRepository creditRepository,
                               TravelPlanRepository travelPlanRepository) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.companyUserRepository = companyUserRepository;
        this.creditRepository = creditRepository;
        this.travelPlanRepository = travelPlanRepository;
    }

    public List<AdminCompanyResponse> findAll() {
        List<Company> companies = companyRepository.findAllActive();
        return companies.stream().map(this::mapToResponse).toList();
    }

    public AdminCompanyResponse findById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return mapToResponse(company);
    }

    public List<AdminCompanyResponse> getEmployees(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        List<Employee> employees = employeeRepository.findAll();
        List<Employee> companyEmployees = employees.stream()
                .filter(e -> e.getCompany() != null && e.getCompany().getId().equals(id))
                .toList();
        
        List<AdminCompanyResponse> result = new ArrayList<>();
        for (Employee emp : companyEmployees) {
            AdminCompanyResponse empResponse = new AdminCompanyResponse();
            empResponse.setId(emp.getId());
            empResponse.setName(emp.getName());
            empResponse.setContactEmail(emp.getEmail());
            empResponse.setContactPhone(emp.getDepartment());
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
        Integer creditsRemaining = company.getTotalCredits() != null ? company.getTotalCredits() : 0;
        Integer creditsUsed = company.getUsedCredits() != null ? company.getUsedCredits() : 0;
        
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

        return new AdminCompanyResponse(
            company.getId(),
            company.getName(),
            company.getIndustry(),
            company.getWebsite(),
            company.getTotalCredits() != null ? company.getTotalCredits() : 0,
            creditsRemaining - creditsUsed,
            (int) plansGenerated,
            (int) activeEmployees,
            billingStatus,
            company.getContractRenewal(),
            tier,
            hrAdmins,
            company.getContactEmail(),
            company.getContactPhone(),
            company.getAddress(),
            company.getCreatedAt()
        );
    }
}
