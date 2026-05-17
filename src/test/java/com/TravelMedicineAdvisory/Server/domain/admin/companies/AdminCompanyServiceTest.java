package com.TravelMedicineAdvisory.Server.domain.admin.companies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.core.utils.RandomNumberGenerator;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.company.Tier;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.AvatarUrlService;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

class AdminCompanyServiceTest {

    private CompanyRepository companyRepository;
    private CreditPlanRepository creditPlanRepository;
    private AdminCompanyService service;

    @BeforeEach
    void setUp() {
        companyRepository = mock(CompanyRepository.class);
        creditPlanRepository = mock(CreditPlanRepository.class);
        CompanyUserRepository companyUserRepository = mock(CompanyUserRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        TravelPlanRepository travelPlanRepository = mock(TravelPlanRepository.class);

        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(companyUserRepository.findAll()).thenReturn(List.of());
        when(employeeRepository.findAll()).thenReturn(List.of());
        when(travelPlanRepository.countByCompanyId(any())).thenReturn(0L);

        service = new AdminCompanyService(
                companyRepository,
                creditPlanRepository,
                employeeRepository,
                companyUserRepository,
                mock(CreditRepository.class),
                mock(RandomNumberGenerator.class),
                travelPlanRepository,
                mock(UserRepository.class),
                mock(RoleRepository.class),
                mock(PasswordEncoder.class),
                mock(QueueService.class),
                mock(AvatarUrlService.class));
    }

    @Test
    void updateMovesCompanyToSelectedCreditPlan() {
        Company company = new Company();
        company.setId(20L);
        company.setName("TMAG Corp");
        company.setTotalCredits(0);
        company.setUsedCredits(0);
        company.setTier(Tier.STANDARD);
        when(companyRepository.findById(20L)).thenReturn(Optional.of(company));

        CreditPlan plan = new CreditPlan();
        plan.setId(30L);
        plan.setCode("ENTERPRISE_ELITE");
        plan.setDisplayName("Enterprise Elite");
        plan.setServiceLevel("PREMIUM");
        when(creditPlanRepository.findByCode("ENTERPRISE_ELITE")).thenReturn(Optional.of(plan));

        AdminCompanyResponse response = service.update(20L, Map.of("planCode", "enterprise_elite"));

        assertThat(company.getCreditPlan()).isSameAs(plan);
        assertThat(company.getPlan()).isEqualTo("ENTERPRISE_ELITE");
        assertThat(company.getTier()).isEqualTo(Tier.ENTERPRISE);
        assertThat(response.getTier()).isEqualTo("enterprise_elite");
    }
}
